import { AfterViewInit, Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';

import { LabelType, Options } from '@angular-slider/ngx-slider';
import { addDays, differenceInDays } from 'date-fns';
/**
 * workaround:
 * Leaflet and leaflet.heat imported here so normally no need to import on other files.
 * But the library is not fully prepared to use in angular, so the following error occurs in the browser console:
 * 
 * Uncaught ReferenceError: L is not defined
 * 
 * B/c of this, in angular.json file both include under scripts in build section in the right order.
 */
import * as L from 'leaflet';
import 'leaflet.heat/dist/leaflet-heat.js';

import { StatisticService } from './statistic.service';

@Component({
  selector: 'lvzviz-statistic',
  templateUrl: './statistic.component.html',
  styleUrls: ['./statistic.component.css'],
})
export class StatisticComponent implements OnInit, AfterViewInit {

  private map!: L.Map;
  dataLoaded = false;

  @ViewChild("chartContainer") chartContainer!: ElementRef;
  @Input() data!: any[];
  chart: any;
  selectedRange = [];
  selectedData = [];

  minValue = 0;
  maxValue = 0;
  options: Options = {
    draggableRange: true,
    stepsArray: [],
    translate: (value: number, label: LabelType): string => {
      switch (label) {
        case LabelType.Low:
          return new Date(value).toDateString();
        case LabelType.High:
          return new Date(value).toDateString();
        default:
          return String(value);
      }
    }
  };

  constructor(private statisticService: StatisticService) { }

  ngOnInit(): void {
    this.statisticService.minmaxdate().subscribe(dates => {
      this.minValue = new Date(dates[0].year, dates[0].monthOfYear - 1, dates[0].dayOfMonth).getTime();
      this.maxValue = new Date(dates[1].year, dates[1].monthOfYear - 1, dates[1].dayOfMonth).getTime();
      const dateDiff = differenceInDays(this.maxValue, this.minValue);
      let stepDates = [...Array(dateDiff).keys()].map(x => addDays(this.minValue, x).getTime());
      this.options = {
        ...this.options,
        stepsArray: stepDates.map((date: number) => {
          return { value: date };
        }),
      };
      /** workaround:
       * The range slider needs steps on building but in this case they are not known before DOM rendering.
       * They need to be loaded first, see above.
       * After this the slider can be displayed.
       */
      this.dataLoaded = true;
    });
  }

  ngAfterViewInit(): void {
    this.initMap();
  }

  initMap(): void {
    /** workaround:
     * Images not loaded correctly so images are copied from node_modules leaflet folder into assets folder.
     */
    L.Icon.Default.imagePath = "assets/leaflet/";

    this.map = L.map('map-statistic').setView([51.339695, 12.373075], 11);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(this.map);

    var heat = (L as any).heatLayer([], {
      radius: 25,
      minOpacity: 0.5
    });
  }
}
