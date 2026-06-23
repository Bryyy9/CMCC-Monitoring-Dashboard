export interface Service {
  id: string;
  name: string;
  url: string;
  category: string;
  status: 'UP' | 'DOWN' | 'UNKNOWN';
  lastCheckedAt: string | null;
  latencyMs: number | null;
  createdAt: string;
}

export interface HealthCheckResult {
  serviceId: string;
  status: 'UP' | 'DOWN' | 'UNKNOWN';
  latencyMs: number | null;
  checkedAt: string;
}

export interface ServiceState {
  services: Service[];
  loading: boolean;
  error: string | null;
  lastSync: Date | null;
  consecutiveFailures: number;
}
