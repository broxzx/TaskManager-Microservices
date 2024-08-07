import {Routes} from '@angular/router';
import {LoginComponent} from "./services/pages/login/login.component";
import {RedirectComponent} from "./services/components/redirect/redirect.component";
import {RegistrationComponent} from "./services/pages/registration/registration.component";
import {KanbanBoardComponent} from "./services/pages/kanban-board/kanban-board.component";
import {MainPageComponent} from "./services/pages/main-page/main-page.component";

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },
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
  },
  {
    path: 'tasks',
    component: KanbanBoardComponent
  },
  {
    path: 'main',
    component: MainPageComponent
  }
];
