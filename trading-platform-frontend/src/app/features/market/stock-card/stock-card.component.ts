import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { StockQuote } from '../../../core/service/market.service';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-stock-card',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule
  ],
  templateUrl: './stock-card.component.html',
  styleUrl: './stock-card.component.scss'
})
export class StockCardComponent {
  @Input() quote!: StockQuote;
  @Input() inWatchlist = false;
  @Output() toggleWatchlist = new EventEmitter<string>();
  @Output() trade = new EventEmitter<string>();

  get isPositive(): boolean {
    return this.quote.change >= 0;
  }

  get changeClass(): string {
    return this.isPositive ? 'positive' : 'negative';
  }

  get changeIcon(): string {
    return this.isPositive ? 'trending_up' : 'trending_down';
  }

  onToggleWatchlist(): void {
    this.toggleWatchlist.emit(this.quote.symbol);
  }

  onTrade(): void {
    this.trade.emit(this.quote.symbol);
  }

  private getNumericValue(value: any): number {
    if (typeof value === 'string') {
      return parseFloat(value) || 0;
    }
    return value || 0;
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(value);
  }

  formatPercent(value: number): string {
    const sign = value >= 0 ? '+' : '';
    return `${sign}${value.toFixed(2)}%`;
  }

  formatNumber(value: any): string {
    const numValue = this.getNumericValue(value);
    return new Intl.NumberFormat('en-US').format(numValue);
  }
}