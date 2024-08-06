import {Component} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {UserRequest} from "../../models/user-request";
import {UserControllerService} from "../../services/user-controller.service";
import {NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-registration',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormsModule,
    NgForOf,
    NgIf
  ],
  templateUrl: './registration.component.html',
  styleUrl: './registration.component.scss'
})
export class RegistrationComponent {

  constructor(private userService: UserControllerService) {

  }

  registrationRequest: UserRequest = {
    username: '',
    email: '',
    firstName: '',
    lastName: '',
    birthDate: '',
    password: ''
  };

  messages: string[] = [];

  public register(): void {
    this.messages = [];

    if (!this.registrationRequest.username) {
      this.messages.push("username");
    }
    if (!this.registrationRequest.email) {
      this.messages.push("email");
    }
    if (!this.registrationRequest.password) {
      this.messages.push("password");
    }

    if (this.messages.length > 0) {
      return;
    }

    this.userService.register({
      body: this.registrationRequest,
    }).subscribe({
      next: value => {
      },
      error: err => {
      }
    });
  }

}
