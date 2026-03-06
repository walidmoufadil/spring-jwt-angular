import { Component, OnInit, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../chat-service';
import { ChatMessage, MessageType, ChatResponse } from '../models/chat-message';
import { HttpClientModule } from '@angular/common/http';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { marked } from 'marked';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './chat.html',
  styleUrl: './chat.css'
})
export class Chat implements OnInit, AfterViewChecked {
  @ViewChild('messageList') private messageList!: ElementRef;

  messages: ChatMessage[] = [];
  newMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private chatService: ChatService,
    private sanitizer: DomSanitizer
  ) {
    marked.setOptions({
      gfm: true,
      breaks: true
    });
  }

  ngOnInit() {
    this.messages.push({
      message: "Bonjour, je suis votre assistant bancaire virtuel. Comment puis-je vous aider aujourd'hui ?",
      isBot: true,
      timestamp: new Date(),
      type: 'text'
    });
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  private scrollToBottom(): void {
    try {
      this.messageList.nativeElement.scrollTop = this.messageList.nativeElement.scrollHeight;
    } catch (err) {}
  }

  formatMessage(message: ChatMessage): SafeHtml {
    if (!message.isBot) {
      return this.sanitizer.bypassSecurityTrustHtml(message.message);
    }

    let formattedContent = message.message;
    // Suppression du titre "Réponse" s'il existe
    formattedContent = formattedContent.replace(/^\*\*Réponse\*\*\s*/g, '');

    try {
      // Utilisation de la méthode synchrone
      const htmlContent = marked.parse(formattedContent, { async: false }) as string;
      const enhancedHtml = this.enhanceHtml(htmlContent, message.type);
      return this.sanitizer.bypassSecurityTrustHtml(enhancedHtml);
    } catch (error) {
      console.error('Erreur lors du parsing Markdown:', error);
      return this.sanitizer.bypassSecurityTrustHtml(formattedContent);
    }
  }

  private enhanceHtml(html: string, type?: MessageType): string {
    let enhanced = html;

    if (type === 'table') {
      // Amélioration des tableaux
      enhanced = enhanced.replace(
        /<table>/g,
        '<div class="table-container"><table class="chat-table">'
      ).replace(
        /<\/table>/g,
        '</table></div>'
      );
    }

    // Amélioration des listes
    if (type === 'list') {
      enhanced = enhanced.replace(
        /<ul>/g,
        '<ul class="chat-list">'
      );
    }

    // Amélioration des liens
    enhanced = enhanced.replace(
      /<a /g,
      '<a class="chat-link" target="_blank" rel="noopener noreferrer" '
    );

    return enhanced;
  }

  async sendMessage() {
    if (!this.newMessage.trim()) return;

    // Ajouter le message de l'utilisateur
    this.messages.push({
      message: this.newMessage,
      isBot: false,
      timestamp: new Date(),
      type: 'text'
    });

    const userMessage = this.newMessage;
    this.newMessage = '';
    this.isLoading = true;

    try {
      this.chatService.sendMessage(userMessage).subscribe({
        next: (response: ChatResponse) => {
          this.messages.push({
            message: response.content,
            isBot: true,
            timestamp: new Date(),
            type: response.type,
            format: response.format
          });
          this.isLoading = false;
        },
        error: () => {
          this.messages.push({
            message: "Désolé, une erreur s'est produite. Veuillez réessayer plus tard.",
            isBot: true,
            timestamp: new Date(),
            type: 'text'
          });
          this.isLoading = false;
        }
      });
    } catch (error) {
      this.isLoading = false;
    }
  }
}
