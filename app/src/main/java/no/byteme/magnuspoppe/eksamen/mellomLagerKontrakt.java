package no.byteme.magnuspoppe.eksamen;

/**
 * En kontrakt laget for å forsikre om at klassen som bruker mellomlagringen
 * faktisk kan håndtere dette.
 * Created by MagnusPoppe on 02/06/2017.
 */

interface mellomLagerKontrakt
{
    void vedKomplettOpplastingAvDestinasjoner(int[] ider);
}
