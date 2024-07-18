/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { StrictHttpResponse } from '../../strict-http-response';
import { RequestBuilder } from '../../request-builder';


export interface ProcessGrantCode$Params {
  code: string;
  scope: string;
  authuser: string;
  prompt: string;
}

export function processGrantCode(http: HttpClient, rootUrl: string, params: ProcessGrantCode$Params, context?: HttpContext): Observable<StrictHttpResponse<void>> {
  const rb = new RequestBuilder(rootUrl, processGrantCode.PATH, 'get');
  if (params) {
    rb.query('code', params.code, {});
    rb.query('scope', params.scope, {});
    rb.query('authuser', params.authuser, {});
    rb.query('prompt', params.prompt, {});
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

processGrantCode.PATH = '/users/grantCode';
