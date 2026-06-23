import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { ServiceCardComponent } from './components/service-card/service-card.component';
import { SummaryBarComponent } from './components/summary-bar/summary-bar.component';
import { ErrorBannerComponent } from './components/error-banner/error-banner.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { StatusBadgeComponent } from './components/status-badge/status-badge.component';
import { HttpClientModule } from '@angular/common/http';

@NgModule({
  declarations: [
    AppComponent,
    ServiceCardComponent,
    SummaryBarComponent,
    ErrorBannerComponent,
    DashboardComponent,
    StatusBadgeComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
