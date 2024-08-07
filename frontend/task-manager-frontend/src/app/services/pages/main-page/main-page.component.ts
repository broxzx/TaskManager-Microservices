import {Component} from '@angular/core';
import {RouterLink, RouterLinkActive, RouterOutlet} from "@angular/router";
import {NgOptimizedImage} from "@angular/common";

@Component({
  selector: 'app-main-page',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    NgOptimizedImage
  ],
  templateUrl: './main-page.component.html',
  styleUrl: './main-page.component.scss'
})
export class MainPageComponent {

  selectItem(event: Event) {
    const selectedItem = event.currentTarget as HTMLElement;
    const previousSelected = document.querySelector('.list-item.selected');

    if (previousSelected) {
      previousSelected.classList.remove('selected');
    }

    selectedItem.classList.add('selected');
  }

}
