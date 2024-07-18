/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { StrictHttpResponse } from '../../strict-http-response';
import { RequestBuilder } from '../../request-builder';

import { ChangePasswordDto } from '../../models/change-password-dto';

export interface ChangePasswordWithNewPassword$Params {
  token: string;
      body: ChangePasswordDto
}

export function changePasswordWithNewPassword(http: HttpClient, rootUrl: string, params: ChangePasswordWithNewPassword$Params, context?: HttpContext): Observable<StrictHttpResponse<void>> {
  const rb = new RequestBuilder(rootUrl, changePasswordWithNewPassword.PATH, 'post');
  if (params) {
    rb.query('token', params.token, {});
    rb.body(params.body, 'application/json');
  }

  return http.request(
    rb.build({ responseType: 'text', accept: '*/*', context })
  ).pipe(
    filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
    map((r: HttpResponse<any>) => {
      return (r as HttpResponse<any>).clone({ body: undefined }) as StrictHttpResponse<void>;
    })
  );
}

changePasswordWithNewPassword.PATH = '/users/changePassword';
