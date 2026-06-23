import { Component, OnInit, OnDestroy } from '@angular/core';
import { MonitoringService } from '../../services/monitoring.service';
import { Service } from '../../models/service.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  vm$ = this.monitoringService.vm$;
  checkingMap: { [id: string]: boolean } = {};
  lastSync: Date | null = null;

  constructor(private monitoringService: MonitoringService) {}

  trackById(index: number, service: Service): string {
    return service.id;
  }

  upCount(services: Service[]): number {
    return services.filter(s => s.status === 'UP').length;
  }

  downCount(services: Service[]): number {
    return services.filter(s => s.status === 'DOWN').length;
  }

  unknownCount(services: Service[]): number {
    return services.filter(s => s.status === 'UNKNOWN').length;
  }

  ngOnInit(): void {
    this.monitoringService.startPolling();
    this.monitoringService.vm$.subscribe(vm => {
      if (vm.lastSync) this.lastSync = vm.lastSync;
    });
  }

  ngOnDestroy(): void {
    this.monitoringService.stopPolling();
  }

  manualRefresh(): void {
    this.monitoringService.retry();
  }

  onRecheck(serviceId: string): void {
    this.checkingMap[serviceId] = true;
    this.monitoringService.forceCheck(serviceId).subscribe({
      next: () => {
        this.checkingMap[serviceId] = false;
      },
      error: (err) => {
        this.checkingMap[serviceId] = false;
        console.error('Recheck failed', err);
      }
    });
  }
}
