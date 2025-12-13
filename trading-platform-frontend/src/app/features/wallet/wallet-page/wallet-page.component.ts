import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { WalletService, Wallet, Transaction } from '../../../core/service/wallet.service'; 
import { AuthService } from '../../../core/service/auth.service'; 
import { DepositDialogComponent } from '../deposit-dialog/deposit-dialog.component';
import { WithdrawDialogComponent } from '../withdraw-dialog/withdraw-dialog.component';

@Component({
  selector: 'app-wallet-page',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    MatSnackBarModule
  ],
  templateUrl: './wallet-page.component.html',
  styleUrl: './wallet-page.component.scss'
})
export class WalletPageComponent implements OnInit {
  wallet: Wallet | null = null;
  transactions: Transaction[] = [];
  loading = true;
  transactionsLoading = false;
  displayedColumns: string[] = ['date', 'type', 'amount', 'balance', 'description'];

  constructor(
    private walletService: WalletService,
    private authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadWallet();
  }

  loadWallet(): void {
    const user = this.authService.getCurrentUser();
    if (!user) return;

    this.loading = true;
    
    this.walletService.getWallet(user.id).subscribe({
      next: (wallet) => {
        this.wallet = wallet;
        this.loading = false;
        this.loadTransactions();
      },
      error: (error) => {
        console.error('Error loading wallet:', error);
        
        if (error.status === 404) {
          this.createWallet();
        } else {
          this.loading = false;
          this.snackBar.open('Failed to load wallet', 'Close', { duration: 3000 });
        }
      }
    });
  }

  createWallet(): void {
    const user = this.authService.getCurrentUser();
    if (!user) return;

    this.walletService.createWallet(user.id, user.role || 'ROLE_BASIC').subscribe({
      next: (wallet) => {
        this.wallet = wallet;
        this.loading = false;
        this.snackBar.open('Wallet created successfully!', 'Close', { duration: 3000 });
        this.loadTransactions();
      },
      error: (error) => {
        console.error('Error creating wallet:', error);
        this.loading = false;
        this.snackBar.open('Failed to create wallet', 'Close', { duration: 3000 });
      }
    });
  }

  loadTransactions(): void {
    const user = this.authService.getCurrentUser();
    if (!user) return;

    this.transactionsLoading = true;
    
    this.walletService.getTransactions(user.id).subscribe({
      next: (transactions) => {
        this.transactions = transactions.sort((a, b) => 
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
        this.transactionsLoading = false;
      },
      error: (error) => {
        console.error('Error loading transactions:', error);
        this.transactionsLoading = false;
      }
    });
  }

  openDepositDialog(): void {
    const dialogRef = this.dialog.open(DepositDialogComponent, {
      width: '400px',
      data: { currentBalance: this.wallet?.balance || 0 }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.performDeposit(result.amount, result.description);
      }
    });
  }

  openWithdrawDialog(): void {
    const dialogRef = this.dialog.open(WithdrawDialogComponent, {
      width: '400px',
      data: { currentBalance: this.wallet?.balance || 0 }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.performWithdraw(result.amount, result.description);
      }
    });
  }

  performDeposit(amount: number, description?: string): void {
    const user = this.authService.getCurrentUser();
    if (!user) return;

    this.walletService.deposit(user.id, amount, description).subscribe({
      next: (transaction) => {
        this.snackBar.open(`Deposited ${this.formatCurrency(amount)} successfully!`, 'Close', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
        this.loadWallet();
      },
      error: (error) => {
        console.error('Deposit error:', error);
        this.snackBar.open('Deposit failed. Please try again.', 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  performWithdraw(amount: number, description?: string): void {
    const user = this.authService.getCurrentUser();
    if (!user) return;

    this.walletService.withdraw(user.id, amount, description).subscribe({
      next: (transaction) => {
        this.snackBar.open(`Withdrew ${this.formatCurrency(amount)} successfully!`, 'Close', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
        this.loadWallet();
      },
      error: (error) => {
        console.error('Withdraw error:', error);
        const message = error.error?.message || 'Withdrawal failed. Please try again.';
        this.snackBar.open(message, 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  getTransactionColor(type: string): string {
    if (type === 'DEPOSIT' || type === 'REFUND') return 'success';
    if (type === 'WITHDRAW' || type === 'ORDER') return 'warn';
    return 'primary';
  }

  getTransactionSign(type: string): string {
    if (type === 'DEPOSIT' || type === 'REFUND') return '+';
    if (type === 'WITHDRAW' || type === 'ORDER') return '-';
    return '';
  }
}