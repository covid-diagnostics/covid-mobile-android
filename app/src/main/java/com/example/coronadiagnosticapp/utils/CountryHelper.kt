package com.example.coronadiagnosticapp.utils

import java.util.*

object CountryHelper {
    fun getCountriesList(): List<Country> {
        val default = Locale.getDefault()
        val isoCountries = Locale.getISOCountries()
        val countries = mutableListOf<Country>()
        for (country in isoCountries) {

            val locale = Locale(default.language, country)
            val iso = locale.isO3Country
            val code = locale.country
            val name = locale.displayCountry

            if (iso != "" && code != "" && name != "") {
                countries.add(Country(iso, code, name))
            }
        }

        countries.sortBy { it.name }
        countries.add(0,Country(default))//Set default locale to top
        return countries
    }
}

class Country(
    val iso: String,
    val code: String,
    val name: String
) {
    constructor(locale: Locale) : this(
        locale.isO3Language,
        locale.country,
        locale.displayCountry
    )

    override fun toString(): String {
        return name
    }
}