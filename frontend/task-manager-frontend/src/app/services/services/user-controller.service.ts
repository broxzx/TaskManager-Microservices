/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';

import { changePasswordWithNewPassword } from '../fn/user-controller/change-password-with-new-password';
import { ChangePasswordWithNewPassword$Params } from '../fn/user-controller/change-password-with-new-password';
import { forgotPassword } from '../fn/user-controller/forgot-password';
import { ForgotPassword$Params } from '../fn/user-controller/forgot-password';
import { getUserIdByToken } from '../fn/user-controller/get-user-id-by-token';
import { GetUserIdByToken$Params } from '../fn/user-controller/get-user-id-by-token';
import { loginUser } from '../fn/user-controller/login-user';
import { LoginUser$Params } from '../fn/user-controller/login-user';
import { processGrantCode } from '../fn/user-controller/process-grant-code';
import { ProcessGrantCode$Params } from '../fn/user-controller/process-grant-code';
import { refreshToken } from '../fn/user-controller/refresh-token';
import { RefreshToken$Params } from '../fn/user-controller/refresh-token';
import { register } from '../fn/user-controller/register';
import { Register$Params } from '../fn/user-controller/register';
import { resetPassword } from '../fn/user-controller/reset-password';
import { ResetPassword$Params } from '../fn/user-controller/reset-password';
import { TokenResponse } from '../models/token-response';
import { updateUserData } from '../fn/user-controller/update-user-data';
import { UpdateUserData$Params } from '../fn/user-controller/update-user-data';
import { UserEntity } from '../models/user-entity';
import {LoginRequest} from "../models/login-request";

@Injectable({ providedIn: 'root' })
export class UserControllerService extends BaseService {
  constructor(config: ApiConfiguration, http: HttpClient) {
    super(config, http);
  }

  private apiUrl = 'http://localhost:8081/users';

  static readonly UpdateUserDataPath = '/users/updateUserData';

  updateUserData$Response(params: UpdateUserData$Params, context?: HttpContext): Observable<StrictHttpResponse<UserEntity>> {
    return updateUserData(this.http, this.rootUrl, params, context);
  }

  updateUserData(params: UpdateUserData$Params, context?: HttpContext): Observable<UserEntity> {
    return this.updateUserData$Response(params, context).pipe(
      map((r: StrictHttpResponse<UserEntity>): UserEntity => r.body)
    );
  }

  static readonly ResetPasswordPath = '/users/resetPassword';

  resetPassword$Response(params: ResetPassword$Params, context?: HttpContext): Observable<StrictHttpResponse<void>> {
    return resetPassword(this.http, this.rootUrl, params, context);
  }

  resetPassword(params: ResetPassword$Params, context?: HttpContext): Observable<void> {
    return this.resetPassword$Response(params, context).pipe(
      map((r: StrictHttpResponse<void>): void => r.body)
    );
  }

  static readonly RegisterPath = '/users/register';

  register$Response(params: Register$Params, context?: HttpContext): Observable<StrictHttpResponse<UserEntity>> {
    return register(this.http, this.rootUrl, params, context);
  }

  register(params: Register$Params, context?: HttpContext): Observable<UserEntity> {
    return this.register$Response(params, context).pipe(
      map((r: StrictHttpResponse<UserEntity>): UserEntity => r.body)
    );
  }

  static readonly RefreshTokenPath = '/users/refreshToken';

  refreshToken$Response(params: RefreshToken$Params, context?: HttpContext): Observable<StrictHttpResponse<TokenResponse>> {
    return refreshToken(this.http, this.rootUrl, params, context);
  }

  refreshToken(params: RefreshToken$Params, context?: HttpContext): Observable<TokenResponse> {
    return this.refreshToken$Response(params, context).pipe(
      map((r: StrictHttpResponse<TokenResponse>): TokenResponse => r.body)
    );
  }

  static readonly LoginUserPath = '/users/login';

  loginUser$Response(params: LoginUser$Params, context?: HttpContext): Observable<StrictHttpResponse<TokenResponse>> {
    return loginUser(this.http, this.rootUrl, params, context);
  }

  loginUser(request: { body: LoginRequest }): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${this.apiUrl}/login`, request.body);
  }

  static readonly ForgotPasswordPath = '/users/forgotPassword';

  forgotPassword$Response(params: ForgotPassword$Params, context?: HttpContext): Observable<StrictHttpResponse<void>> {
    return forgotPassword(this.http, this.rootUrl, params, context);
  }


  forgotPassword(params: ForgotPassword$Params, context?: HttpContext): Observable<void> {
    return this.forgotPassword$Response(params, context).pipe(
      map((r: StrictHttpResponse<void>): void => r.body)
    );
  }

  static readonly ChangePasswordWithNewPasswordPath = '/users/changePassword';

  changePasswordWithNewPassword$Response(params: ChangePasswordWithNewPassword$Params, context?: HttpContext): Observable<StrictHttpResponse<void>> {
    return changePasswordWithNewPassword(this.http, this.rootUrl, params, context);
  }


  changePasswordWithNewPassword(params: ChangePasswordWithNewPassword$Params, context?: HttpContext): Observable<void> {
    return this.changePasswordWithNewPassword$Response(params, context).pipe(
      map((r: StrictHttpResponse<void>): void => r.body)
    );
  }

  static readonly ProcessGrantCodePath = '/users/grantCode';

  processGrantCode$Response(params: ProcessGrantCode$Params, context?: HttpContext): Observable<StrictHttpResponse<void>> {
    return processGrantCode(this.http, this.rootUrl, params, context);
  }

  processGrantCode(params: ProcessGrantCode$Params, context?: HttpContext): Observable<void> {
    return this.processGrantCode$Response(params, context).pipe(
      map((r: StrictHttpResponse<void>): void => r.body)
    );
  }

  static readonly GetUserIdByTokenPath = '/users/getUserIdByToken';

  getUserIdByToken$Response(params: GetUserIdByToken$Params, context?: HttpContext): Observable<StrictHttpResponse<string>> {
    return getUserIdByToken(this.http, this.rootUrl, params, context);
  }

  getUserIdByToken(params: GetUserIdByToken$Params, context?: HttpContext): Observable<string> {
    return this.getUserIdByToken$Response(params, context).pipe(
      map((r: StrictHttpResponse<string>): string => r.body)
    );
  }

}
