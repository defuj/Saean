package com.saean.app.helper

class MyComparator : Comparator<String?> {
    private val items = arrayOf(
        "Minggu",
        "Senin",
        "Selasa",
        "Rabu",
        "Kamis",
        "Jumat",
        "Sabtu"
    )

    override fun compare(a: String?, b: String?): Int {
        var ai = items.size
        var bi = items.size
        for (i in items.indices) {
            if (items[i].equals(a, ignoreCase = true)) ai = i
            if (items[i].equals(b, ignoreCase = true)) bi = i
        }
        return ai - bi
    }
}