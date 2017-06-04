package no.byteme.magnuspoppe.eksamen;

/**
 * En kontrakt laget for å forsikre om at klassen som bruker mellomlagringen
 * faktisk kan håndtere dette.
 *
 * Denne passer på at objekter faktisk er opplastet til tjeneren før de
 * slettes lokalt fra enheten. Dette er veldig viktig for at appen skal
 * være trygg i bruk.
 * Created by MagnusPoppe on 02/06/2017.
 */

interface mellomLagerKontrakt
{
    void vedKomplettOpplastingAvDestinasjoner(int[] ider);
}
