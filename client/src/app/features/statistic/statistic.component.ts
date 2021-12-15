import { AfterViewInit, Component, OnInit } from '@angular/core';
import { Options } from '@angular-slider/ngx-slider';

import { addDays, differenceInDays } from 'date-fns';
import { forkJoin, map } from 'rxjs';
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

  minValue = 0;
  maxValue = 0;
  options!: Options;

  constructor(private statisticService: StatisticService) { }

  ngOnInit(): void {
    const minmaxdate$ = this.statisticService.fetchDates('minmaxdate');
    const last7days$ = this.statisticService.fetchDates('last7days');

    forkJoin([minmaxdate$, last7days$]).subscribe((dates) => {
      const minStepValue = this.determineDateValue(dates[0][0]).getTime();
      const maxStepValue = this.determineDateValue(dates[0][1]).getTime();
      const minDateValue = this.determineDateValue(dates[1][0]);
      const maxDateValue = this.determineDateValue(dates[1][1]);
      this.minValue = minDateValue.getTime();
      this.maxValue = maxDateValue.getTime();
      this.options = {
        draggableRange: true,
        translate: (value: number) => new Date(value).toDateString(),
        stepsArray: this.generateSliderSteps(minStepValue, maxStepValue).map((date: number) => ({ value: date })),
      };
      /** workaround:
       * The range slider needs steps on building but in this case they are not known before DOM rendering.
       * They need to be loaded first, see above.
       * After this the slider can be displayed.
       */
      this.dataLoaded = true;
      this.statisticService.fetch(minDateValue.toISOString(), maxDateValue.toISOString()).pipe(
        map(data => data.content),
      ).subscribe(data => this.addToMap(data));
    });
  }

  ngAfterViewInit(): void {
    this.initMap();
  }

  /**
   * Initialize map.
   */
  initMap(): void {
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

  private addToMap(content: Content[]): void {
    var latlng: any[] = [];
    content.forEach((c) => {
      if (c.coords) {
        latlng.push([c.coords.lat, c.coords.lon]);
      }
    });
    this.heat.setLatLngs(latlng);
    // TODO: what does this line do?
    // currently now functional b/c _size is not defined
    //if (this.map._size.y === 0) { } else {
      this.map.addLayer(this.heat);
    //}
  };

  private determineDateValue(date: DateTime): Date {
    return new Date(date.year, date.monthOfYear - 1, date.dayOfMonth);
  }

  private generateSliderSteps(minValue: number, maxValue: number): number[] {
    const dateDiff = differenceInDays(maxValue, minValue);
    return [...Array(dateDiff).keys()].map(x => addDays(minValue, x).getTime());
  }
}
