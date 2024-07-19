import {Component, Inject, OnInit, PLATFORM_ID} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {isPlatformBrowser} from "@angular/common";

@Component({
  selector: 'app-redirect',
  standalone: true,
  imports: [],
  templateUrl: './redirect.component.html',
  styleUrl: './redirect.component.scss'
})
export class RedirectComponent implements OnInit {

  constructor(private http: HttpClient, @Inject(PLATFORM_ID) private platformId: Object) {
  }

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.http.get('https://accounts.google.com/o/oauth2/v2/auth?redirect_uri=http://localhost:8080/users/grantCode&response_type=code&client_id=863784785750-2ol5aj5su9a5v0t5uabium81hdpn4nlc.apps.googleusercontent.com&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile+openid&access_type=offline', { responseType: 'text' }).subscribe(
        (url: string) => {
          window.location.href = url;
        },
        (error) => {
          console.error(`Redirect failed ${error}`);
        }
      );
    }
  }


}
