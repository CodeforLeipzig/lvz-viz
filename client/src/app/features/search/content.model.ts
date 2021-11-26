import { Cords } from './cords.model';

export interface Content {
  id: string;
  url: string;
  title: string;
  article: string;
  snippet: string;
  copyright: string;
  datePublished: string;
  coords: Cords;
}
