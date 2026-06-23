import { Component, Input, OnChanges } from '@angular/core';
import { Service } from '../../models/service.model';

@Component({
  selector: 'app-summary-bar',
  templateUrl: './summary-bar.component.html',
  styleUrls: ['./summary-bar.component.css']
})
export class SummaryBarComponent implements OnChanges {
  @Input() services: Service[] = [];

  total = 0;
  up = 0;
  down = 0;
  unknown = 0;

  ngOnChanges() {
    this.total = this.services.length;
    this.up = this.services.filter(s => s.status === 'UP').length;
    this.down = this.services.filter(s => s.status === 'DOWN').length;
    this.unknown = this.services.filter(s => s.status === 'UNKNOWN').length;
  }
}
