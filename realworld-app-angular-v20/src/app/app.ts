import { Component, ChangeDetectionStrategy, inject, signal, effect } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth/auth-service';
// Removido HttpErrorResponse e formatErrors, pois o logout não fará mais chamadas de API que possam gerar esses erros aqui.
// import { HttpErrorResponse } from '@angular/common/http';
// import { formatErrors } from './shared/formatting';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
} )
export class App {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  // Signals for UI State
  // isSubmitting e errors não são mais necessários aqui para o logout, pois ele não é assíncrono.
  // readonly isSubmitting = signal(false);
  // readonly errors = signal<string[]>([]);
  
  readonly currentUser = this.authService.currentUser;

  logout() {
    // Chamada direta ao método logout do AuthService, que agora é síncrono e já trata a navegação.
    this.authService.logout();
    // A navegação para /login já é tratada dentro do authService.logout()
    // this.router.navigate(['/login']); 
  }
}