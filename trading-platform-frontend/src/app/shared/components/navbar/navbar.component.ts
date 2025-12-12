import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { AuthService } from '../../../core/service/auth.service'; 
import { WalletService } from '../../../core/service/wallet.service';
import { User } from '../../../core/models/user.model';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatBadgeModule,
    MatDividerModule
  ],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent implements OnInit {
  currentUser: User | null = null;
  balance: number = 0;
  balanceLoading: boolean = false;

  constructor(
    private authService: AuthService,
    private walletService: WalletService,
    private router: Router
  ) {}

  ngOnInit(): void {

    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user) {
        this.loadBalance();
      }
    });

    this.walletService.balance$.subscribe(balance => {
      this.balance = balance;
    });
  }

  loadBalance(): void {
    if (!this.currentUser) return;

    this.balanceLoading = true;
    this.walletService.getBalance(this.currentUser.id).subscribe({
      next: (response) => {
        this.balance = response.balance;
        this.balanceLoading = false;
      },
      error: (error) => {
        console.error('Failed to load balance:', error);
        this.balanceLoading = false;
        this.balance = 0;
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }
}