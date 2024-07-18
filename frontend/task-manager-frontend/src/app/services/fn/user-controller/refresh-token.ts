/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { StrictHttpResponse } from '../../strict-http-response';
import { RequestBuilder } from '../../request-builder';

import { TokenResponse } from '../../models/token-response';

export interface RefreshToken$Params {
  token: string;
}

export function refreshToken(http: HttpClient, rootUrl: string, params: RefreshToken$Params, context?: HttpContext): Observable<StrictHttpResponse<TokenResponse>> {
  const rb = new RequestBuilder(rootUrl, refreshToken.PATH, 'post');
  if (params) {
    rb.query('token', params.token, {});
  }

  return http.request(
    rb.build({ responseType: 'blob', accept: '*/*', context })
  ).pipe(
    filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
    map((r: HttpResponse<any>) => {
      return r as StrictHttpResponse<TokenResponse>;
    })
  );
}

refreshToken.PATH = '/users/refreshToken';
