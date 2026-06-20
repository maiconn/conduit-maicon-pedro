import { Component, inject, input, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { EditUser, UserService } from '../../core/services/user-service';
import { User } from '../../core/models/auth/user'; // Importação adicionada

@Component({
  selector: 'app-settings',
  imports: [ReactiveFormsModule],
  templateUrl: './settings.html',
  styleUrl: './settings.css',
})
export class Settings implements OnInit {
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);

  selectedFile: File | null = null;
  readonly imagePreview = signal<string | null>(null);

  readonly isSubmitting = signal<boolean>(false);

  readonly userForm = this.fb.nonNullable.group({
    email: this.fb.nonNullable.control('', [Validators.required, Validators.email]), // Corrigido
    bio: this.fb.control<string | null>(null), // bio é opcional, então Validators.required foi removido
  });

  ngOnInit(): void {
    this.fetchUser();
  }

  fetchUser() {
    this.userService.editUser().subscribe({
      next: (user: User) => {
        this.userForm.patchValue({
          email: user.email,
          bio: user.bio ?? '' // Garante que bio seja string ou vazio
        });
        if(user.image) {
          this.imagePreview.set(user.image);
        }
      },
      error: (err: any) => {
        console.error(err);
      }
    });
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if(input.files && input.files.length > 0) {
      const file = input.files[0];
      this.selectedFile = file;

      // Create a preview
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview.set(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  }

  onSubmit() {
    if(this.userForm.invalid) return;

    this.isSubmitting.set(true);

    const formValue = this.userForm.getRawValue();
    const userUpdate: Partial<User> = {
      email: formValue.email,
      bio: formValue.bio,
      image: this.imagePreview() ?? null // Garante que image seja string ou null
    };

    this.userService.updateUser(userUpdate).subscribe({
      next: (user) => {
        this.isSubmitting.set(false);
        this.router.navigate(['/profile', user.username]);
      },
      error: (err) => {
        console.error(err);
        this.isSubmitting.set(false);
      },
    });
  }
}