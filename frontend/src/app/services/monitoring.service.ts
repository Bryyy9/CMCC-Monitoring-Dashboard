import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, timer, of, Subject, throwError } from 'rxjs';
import { catchError, exhaustMap, map, shareReplay, switchMap, takeUntil, tap, filter } from 'rxjs/operators';
import { Service, ServiceState, HealthCheckResult } from '../models/service.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MonitoringService {
  private state$ = new BehaviorSubject<ServiceState>({
    services: [],
    loading: false,
    error: null,
    lastSync: null,
    consecutiveFailures: 0,
  });

  readonly vm$ = this.state$.asObservable();

  private destroy$ = new Subject<void>();
  private pollingActive = false;

  constructor(private http: HttpClient) {}

  private get state(): ServiceState {
    return this.state$.getValue();
  }

  private patchState(partial: Partial<ServiceState>) {
    this.state$.next({ ...this.state, ...partial });
  }

  startPolling() {
    if (this.pollingActive) return;
    this.pollingActive = true;
    this.patchState({ loading: true, error: null });

    timer(0, environment.pollIntervalMs).pipe(
      takeUntil(this.destroy$),
      switchMap(() => this.http.get<Service[]>(`${environment.apiBaseUrl}/services`).pipe(
        map(services => {
          this.patchState({
            services,
            loading: false,
            error: null,
            lastSync: new Date(),
            consecutiveFailures: 0
          });
        }),
        catchError(error => {
          this.handlePollingError(error);
          return of(null);
        })
      ))
    ).subscribe();
  }

  stopPolling() {
    this.pollingActive = false;
    this.destroy$.next();
  }

  retry() {
    this.patchState({ error: null, consecutiveFailures: 0 });
    this.startPolling();
  }

  refreshNow(): Observable<HealthCheckResult[] | null> {
    return this.http.post<HealthCheckResult[]>(`${environment.apiBaseUrl}/services/check`, {}).pipe(
      tap(results => {
        const updatedServices = this.state.services.map(s => {
          const result = results.find(r => r.serviceId === s.id);
          return result ? { ...s, status: result.status, latencyMs: result.latencyMs, lastCheckedAt: result.checkedAt } : s;
        });
        this.patchState({
          services: updatedServices,
          loading: false,
          error: null,
          lastSync: new Date(),
          consecutiveFailures: 0
        });
      }),
      catchError(error => {
        this.handlePollingError(error);
        return of(null);
      })
    );
  }

  private handlePollingError(error: any) {
    const failures = this.state.consecutiveFailures + 1;
    if (failures >= environment.maxConsecutiveFailures) {
      this.stopPolling();
      this.patchState({
        error: 'Backend is unreachable',
        loading: false,
        consecutiveFailures: failures
      });
      // Try again after retryDelayMs
      setTimeout(() => this.retry(), environment.retryDelayMs);
    } else {
      this.patchState({ consecutiveFailures: failures, loading: false });
    }
  }

  forceCheck(id: string): Observable<HealthCheckResult> {
    return this.http.post<HealthCheckResult>(`${environment.apiBaseUrl}/services/${id}/check`, {}).pipe(
      tap((result) => {
        // Update local state
        const updatedServices = this.state.services.map(s => 
          s.id === id ? { ...s, status: result.status, latencyMs: result.latencyMs, lastCheckedAt: result.checkedAt } : s
        );
        this.patchState({ services: updatedServices });
      })
    );
  }
}
