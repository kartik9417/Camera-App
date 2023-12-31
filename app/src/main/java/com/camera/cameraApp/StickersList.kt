package com.camera.cameraApp

object StickersList {

    private var stickerList = listOf(
        "\uD83D\uDE00", "\uD83D\uDE01", "\uD83D\uDE02", "\uD83D\uDE03", "\uD83D\uDE04",
        "\uD83D\uDE05", "\uD83D\uDE06", "\uD83D\uDE07", "\uD83D\uDE08", "\uD83D\uDE09",
        "\uD83D\uDE0A", "\uD83D\uDE0B", "\uD83D\uDE0C", "\uD83D\uDE0D", "\uD83D\uDE0E",
        "\uD83D\uDE0F", "\uD83D\uDE10", "\uD83D\uDE11", "\uD83D\uDE12", "\uD83D\uDE13",
        "\uD83D\uDE14", "\uD83D\uDE15", "\uD83D\uDE16", "\uD83D\uDE17", "\uD83D\uDE18",
        "\uD83D\uDE19", "\uD83D\uDE1A", "\uD83D\uDE1B", "\uD83D\uDE1C", "\uD83D\uDE1D",
        "\uD83D\uDE1E", "\uD83D\uDE1F", "\uD83D\uDE20", "\uD83D\uDE21", "\uD83D\uDE22",
        "\uD83D\uDE23", "\uD83D\uDE24", "\uD83D\uDE25", "\uD83D\uDE26", "\uD83D\uDE27",
        "\uD83D\uDE28", "\uD83D\uDE29", "\uD83D\uDE2A", "\uD83D\uDE2B", "\uD83D\uDE2C",
        "\uD83D\uDE2D", "\uD83D\uDE2E", "\uD83D\uDE2F", "\uD83D\uDE30", "\uD83D\uDE31",
        "\uD83D\uDE32", "\uD83D\uDE33", "\uD83D\uDE34", "\uD83D\uDE35", "\uD83D\uDE36",
        "\uD83D\uDE37", "\uD83D\uDE38", "\uD83D\uDE39", "\uD83D\uDE3A", "\uD83D\uDE3B",
        "\uD83D\uDE3C", "\uD83D\uDE3D", "\uD83D\uDE3E", "\uD83D\uDE3F", "\uD83D\uDE40",
        "\uD83D\uDE41", "\uD83D\uDE42", "\uD83D\uDE43", "\uD83D\uDE44", "\uD83D\uDE45",
        "\uD83D\uDE46", "\uD83D\uDE47", "\uD83D\uDE48", "\uD83D\uDE49", "\uD83D\uDE4A",
        "\uD83D\uDE4B", "\uD83D\uDE4C", "\uD83D\uDE4D", "\uD83D\uDE4E", "\uD83D\uDE4F",
        "\uD83D\uDE80", "\uD83D\uDE81", "\uD83D\uDE82", "\uD83D\uDE83", "\uD83D\uDE84",
        "\uD83D\uDE85", "\uD83D\uDE86", "\uD83D\uDE87", "\uD83D\uDE88", "\uD83D\uDE89",
        "\uD83D\uDE8A", "\uD83D\uDE8B", "\uD83D\uDE8C", "\uD83D\uDE8D", "\uD83D\uDE8E",
        "\uD83D\uDE8F", "\uD83D\uDE90", "\uD83D\uDE91", "\uD83D\uDE92", "\uD83D\uDE93",
        "\uD83D\uDE00", "\uD83D\uDE01", "\uD83D\uDE02", "\uD83D\uDE03", "\uD83D\uDE04",
        "\uD83D\uDE05", "\uD83D\uDE06", "\uD83D\uDE07", "\uD83D\uDE08", "\uD83D\uDE09",
        "\uD83D\uDE0A", "\uD83D\uDE0B", "\uD83D\uDE0C", "\uD83D\uDE0D", "\uD83D\uDE0E",
        "\uD83D\uDE0F", "\uD83D\uDE10", "\uD83D\uDE11", "\uD83D\uDE12", "\uD83D\uDE13",
        "\uD83D\uDE14", "\uD83D\uDE15", "\uD83D\uDE16", "\uD83D\uDE17", "\uD83D\uDE18",
        "\uD83D\uDE19", "\uD83D\uDE1A", "\uD83D\uDE1B", "\uD83D\uDE1C", "\uD83D\uDE1D",
        "\uD83D\uDE1E", "\uD83D\uDE1F", "\uD83D\uDE20", "\uD83D\uDE21", "\uD83D\uDE22",
        "\uD83D\uDE23", "\uD83D\uDE24", "\uD83D\uDE25", "\uD83D\uDE26", "\uD83D\uDE27",
        "\uD83D\uDE28", "\uD83D\uDE29", "\uD83D\uDE2A", "\uD83D\uDE2B", "\uD83D\uDE2C",
        "\uD83D\uDE2D", "\uD83D\uDE2E", "\uD83D\uDE2F", "\uD83D\uDE30", "\uD83D\uDE31",
        "\uD83D\uDE32", "\uD83D\uDE33", "\uD83D\uDE34", "\uD83D\uDE35", "\uD83D\uDE36",
        "\uD83D\uDE37", "\uD83D\uDE38", "\uD83D\uDE39", "\uD83D\uDE3A", "\uD83D\uDE3B",
        "\uD83D\uDE3C", "\uD83D\uDE3D", "\uD83D\uDE3E", "\uD83D\uDE3F", "\uD83D\uDE40",
        "\uD83D\uDE41", "\uD83D\uDE42", "\uD83D\uDE43", "\uD83D\uDE44", "\uD83D\uDE45",
        "\uD83D\uDE46", "\uD83D\uDE47", "\uD83D\uDE48", "\uD83D\uDE49", "\uD83D\uDE4A",
        "\uD83D\uDE4B", "\uD83D\uDE4C", "\uD83D\uDE4D", "\uD83D\uDE4E", "\uD83D\uDE4F",
        "\uD83D\uDE80", "\uD83D\uDE81", "\uD83D\uDE82", "\uD83D\uDE83", "\uD83D\uDE84",
        "\uD83D\uDE85", "\uD83D\uDE86", "\uD83D\uDE87", "\uD83D\uDE88", "\uD83D\uDE89",
        "\uD83D\uDE8A", "\uD83D\uDE8B", "\uD83D\uDE8C", "\uD83D\uDE8D", "\uD83D\uDE8E",
        "\uD83D\uDE8F", "\uD83D\uDE90", "\uD83D\uDE91", "\uD83D\uDE92", "\uD83D\uDE93",
        "\uD83D\uDE94", "\uD83D\uDE95", "\uD83D\uDE96", "\uD83D\uDE97", "\uD83D\uDE98",
        "\uD83D\uDE99", "\uD83D\uDE9A", "\uD83D\uDE9B", "\uD83D\uDE9C", "\uD83D\uDE9D",
        "\uD83D\uDE9E", "\uD83D\uDE9F", "\uD83D\uDEA0", "\uD83D\uDEA1", "\uD83D\uDEA2"
    )

    fun getList(): List<String> {
        return stickerList
    }
}