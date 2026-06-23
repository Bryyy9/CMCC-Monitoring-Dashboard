import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Service } from '../../models/service.model';

@Component({
  selector: 'app-service-card',
  templateUrl: './service-card.component.html',
  styleUrls: ['./service-card.component.css']
})
export class ServiceCardComponent {
  @Input() service!: Service;
  @Input() checking = false;
  @Output() recheck = new EventEmitter<string>();

  errorMsg: string | null = null;

  onRecheck() {
    this.errorMsg = null;
    this.recheck.emit(this.service.id);
  }
}
