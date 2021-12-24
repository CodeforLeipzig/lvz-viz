import { AfterViewInit, Component, OnInit } from '@angular/core';
import { ChangeContext, Options } from '@angular-slider/ngx-slider';

import { addDays, subDays } from 'date-fns';
import { forkJoin } from 'rxjs';
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

import { Content } from '../search/content.model';
import { DateTime } from './date-time.model';
import { StatisticService } from './statistic.service';

@Component({
  selector: 'lvzviz-statistic',
  templateUrl: './statistic.component.html',
  styleUrls: ['./statistic.component.css'],
})
export class StatisticComponent implements OnInit, AfterViewInit {

  private map!: L.Map;
  private heat: any;
  dataLoaded = false;

  private minBound = 0;
  private maxBound = 0;
  minValue = 0;
  maxValue = 0;
  options!: Options;

  constructor(private statisticService: StatisticService) { }

  ngOnInit(): void {
    this.initSlider();
  }

  ngAfterViewInit(): void {
    this.initMap();
  }

  /**
   * Initialize slider with start and end dates by setting steps, set minVlaue and maxValue (slider values).
   * 
   * Gets the min and max dates of articles and set start and end of slider with these values.
   * Gets the dates from the last 7 days and set slider values.
   */
  private initSlider(): void {
    forkJoin([
      this.statisticService.fetchDates('minmaxdate'),
      this.statisticService.fetchDates('last7days')
    ]).subscribe(([minmaxdate, last7days]: [DateTime[], DateTime[]]) => {
      this.minBound = this.statisticService.determineDateValue(minmaxdate[0]).getTime();
      this.maxBound = this.statisticService.determineDateValue(minmaxdate[1]).getTime();
      this.minValue = this.statisticService.determineDateValue(last7days[0]).getTime();
      this.maxValue = this.statisticService.determineDateValue(last7days[1]).getTime();
      this.options = {
        draggableRange: true,
        translate: (value: number) => new Date(value).toDateString(),
        stepsArray: this.statisticService.generateSliderSteps(this.minBound, this.maxBound).map((date: number) => ({ value: date })),
      };
      /** workaround:
       * The range slider needs steps on building but in this case they are not known before DOM rendering.
       * They need to be loaded first, see above.
       * After this the slider can be displayed.
       */
      this.dataLoaded = true;
      this.loadContent();
    });
  }

  /**
   * Initialize map.
   */
  private initMap(): void {
    /** workaround:
     * Images not loaded correctly so images are copied from node_modules leaflet folder into assets folder.
     */
    L.Icon.Default.imagePath = "assets/leaflet/";

    this.map = L.map('map-statistic').setView([51.339695, 12.373075], 11);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(this.map);

    this.heat = (L as any).heatLayer([], {
      radius: 25,
      minOpacity: 0.5
    });
  }

  /**
   * Loads content from backend with specified date values, extracts the content of articles and add these to the map.
   */
  private loadContent(): void {
    this.statisticService.fetch(this.minValue, this.maxValue).subscribe(content => this.addToMap(content));
  }

  /**
   * Integrates content as heatpoints returned from the server into the initialized map.
   * 
   * @param content
   */
  private addToMap(content: Content[]): void {
    var latlng: any[] = [];
    content.forEach((c) => {
      if (c.coords) {
        latlng.push([c.coords.lat, c.coords.lon]);
      }
    });
    this.heat.setLatLngs(latlng);
    /**
     * inpercima:
     * I don't know what this check does.
     * Maybe the check can be removed.
     */
    if (this.map.getSize().y !== 0) {
      this.map.addLayer(this.heat);
    }
  };

  onUserChangeEnd(changeContext: ChangeContext): void {
    this.loadContent();
  }

  runBackward(event: Event): void {
    if (this.minValue > this.minBound) {
      this.runAndLoad(true);
    }
  }

  runForward($event: Event): void {
    if (this.maxValue < this.maxBound) {
      this.runAndLoad(true);
    }
  }

  private runAndLoad(isBackward: boolean): void {
    this.minValue = isBackward ? subDays(this.minValue, 1).getTime() : addDays(this.minValue, 1).getTime();
    this.maxValue = isBackward ? subDays(this.maxValue, 1).getTime() : addDays(this.maxValue, 1).getTime();
    this.loadContent();
  }
}
