import { Routes } from '@angular/router';
import {LoginComponent} from "./services/pages/login/login.component";
import {RedirectComponent} from "./services/components/redirect/redirect.component";

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'google',
    component: RedirectComponent
  }
];
