package no.byteme.magnuspoppe.eksamen;

/**
 * Siden opplastingsmetoden for bilder er blitt testet og
 * blitt bevist ustabil. Det er derfor ekstremt viktig at
 * opplastningen skjer med trygg callback i tilfelle noe
 * skjer under opplastingen.
 *
 * Kontrakten skal forsikre at bildet ikke blir slettet
 * fra lokal enhet før det er ferdig opplastet.
 *
 * Created by MagnusPoppe on 02/06/2017.
 */

interface mellomLagerBildeKontrakt
{
    void vedKomplettOpplastingAvBilde(boolean status);
}
