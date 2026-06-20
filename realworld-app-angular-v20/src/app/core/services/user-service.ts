import { inject, Injectable } from '@angular/core';
import { ApiService } from './api-service';
import { map } from 'rxjs';
import { User } from '../models/auth/user';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly api = inject(ApiService);

  editUser() {
    return this.api.get<UserResponse>('/user').pipe(
      map(response => response.user)
    );
  }
  // Método updateUser agora aceita Partial<User> e usa PUT
  updateUser(userUpdate: Partial<User>) {
    return this.api.put<UserResponse>(`/user`, { user: userUpdate }).pipe(
      map(response => response.user)
    );
  }
}

interface UserResponse {
  user: User;
};

export interface EditUser {
  username: string;
  email: string;
  bio: string | null;
  image: string | null;
};