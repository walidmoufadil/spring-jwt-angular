export type MessageType = 'text' | 'table' | 'list';
export type MessageFormat = 'markdown' | 'html';

export interface ChatMessage {
    message: string;
    isBot: boolean;
    timestamp: Date;
    type?: MessageType;
    format?: MessageFormat;
}

export interface ChatResponse {
    content: string;
    type: MessageType;
    format: MessageFormat;
}
