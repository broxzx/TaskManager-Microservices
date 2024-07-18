import {Component} from '@angular/core';
import {FormsModule} from "@angular/forms";
import {LoginRequest} from "../../models/login-request";
import {Router} from "@angular/router";
import {UserControllerService} from "../../services/user-controller.service";
import {TokenStorageService} from "../../services/token-storage.service";
import {TokenResponse} from "../../models/token-response";

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    FormsModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {

  loginRequest: LoginRequest = {username: '', password: '', rememberMe: false};
  errors: Array<String> = [];

  constructor(private router: Router, private userControllerService: UserControllerService, private tokenStorageService: TokenStorageService) {
  }

  login() {
    console.log(this.loginRequest.username)
    console.log(this.loginRequest.password)
    this.userControllerService.loginUser({
      body: this.loginRequest
    }).subscribe({
      next: (response: TokenResponse) => {
        console.log(response.accessToken)
        console.log(response.refreshToken)
        this.tokenStorageService.saveToken(response.accessToken ?? '');
        this.tokenStorageService.saveRefreshToken(response.refreshToken ?? '');
        this.router.navigate(['tasks']);
      },
      error: err => {
        console.log(err)
        this.errors.push(err.error.message || 'An error occurred during login');
      }
    })
  }

}
