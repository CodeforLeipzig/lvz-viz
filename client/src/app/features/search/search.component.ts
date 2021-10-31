import { Component, OnInit } from '@angular/core';

import { SearchService } from './search.service';
import { pipe } from 'rxjs';
import { MatTableDataSource } from '@angular/material/table';

@Component({
  selector: 'lvzviz-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {

  displayedColumns: string[] = ['title', 'publication'];
  dataSource = new MatTableDataSource();
  expandedElement!: null;

  constructor(private searchService: SearchService) { }

  ngOnInit(): void {
    this.searchService.getx().subscribe((response: any) => {
      this.dataSource = new MatTableDataSource(response.content);
    });
  }
}
