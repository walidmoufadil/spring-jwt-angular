import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChatResponse } from './models/chat-message';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = 'http://localhost:9090/chat';

  constructor(private http: HttpClient) { }

  sendMessage(message: string): Observable<ChatResponse> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this.http.post<ChatResponse>(`${this.apiUrl}/send`, { message }, {
      headers: headers
    });
  }
}
