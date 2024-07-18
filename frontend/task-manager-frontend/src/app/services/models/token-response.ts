/* tslint:disable */
/* eslint-disable */
import { UserResponse } from './user-response';
export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  userResponse: UserResponse;
}
