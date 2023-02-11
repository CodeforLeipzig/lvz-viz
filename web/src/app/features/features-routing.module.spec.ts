import { FeaturesRoutingModule } from './features-routing.module';

describe('FeaturesRoutingModule', () => {
  let featuresRoutingModule: FeaturesRoutingModule;

  beforeEach(() => {
    featuresRoutingModule = new FeaturesRoutingModule();
  });

  it('should create an instance', () => {
    expect(featuresRoutingModule).toBeTruthy();
  });
});
