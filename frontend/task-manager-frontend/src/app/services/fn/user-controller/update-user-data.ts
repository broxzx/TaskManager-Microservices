/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { StrictHttpResponse } from '../../strict-http-response';
import { RequestBuilder } from '../../request-builder';

import { UserEntity } from '../../models/user-entity';
import { UserRequest } from '../../models/user-request';

export interface UpdateUserData$Params {
  Authorization: string;
      body: UserRequest
}

export function updateUserData(http: HttpClient, rootUrl: string, params: UpdateUserData$Params, context?: HttpContext): Observable<StrictHttpResponse<UserEntity>> {
  const rb = new RequestBuilder(rootUrl, updateUserData.PATH, 'put');
  if (params) {
    rb.header('Authorization', params.Authorization, {});
    rb.body(params.body, 'application/json');
  }

  return http.request(
    rb.build({ responseType: 'blob', accept: '*/*', context })
  ).pipe(
    filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
    map((r: HttpResponse<any>) => {
      return r as StrictHttpResponse<UserEntity>;
    })
  );
}

updateUserData.PATH = '/users/updateUserData';
