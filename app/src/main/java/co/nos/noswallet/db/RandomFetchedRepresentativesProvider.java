package co.nos.noswallet.db;

import javax.inject.Inject;

import co.nos.noswallet.model.PreconfiguredRepresentatives;

public class RandomFetchedRepresentativesProvider implements RepresentativesProvider{

   @Inject RandomFetchedRepresentativesProvider(){}
    @Override
    public String provideRepresentative() {
        return PreconfiguredRepresentatives.getRepresentative();
    }
}
