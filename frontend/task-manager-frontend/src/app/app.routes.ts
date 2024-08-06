import {Routes} from '@angular/router';
import {LoginComponent} from "./services/pages/login/login.component";
import {RedirectComponent} from "./services/components/redirect/redirect.component";
import {RegistrationComponent} from "./services/pages/registration/registration.component";

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'google',
    component: RedirectComponent
  },
  {
    path: 'registration',
    component: RegistrationComponent
  }
];
