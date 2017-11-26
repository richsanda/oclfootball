<xsl:transform
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"

        exclude-result-prefixes="#all"

        version="2.0">

    <xsl:output method="xml"
                omit-xml-declaration="yes"
                indent="yes"/>

    <xsl:strip-space elements="*"/>

    <xsl:param name="season"/>
    <xsl:param name="scoringPeriod"/>
    <xsl:param name="team"/>

    <xsl:template match="/">
        <game>
            <xsl:if test="$season">
                <xsl:attribute name="season"><xsl:value-of select="$season"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$scoringPeriod">
                <xsl:attribute name="scoringPeriod"><xsl:value-of select="$scoringPeriod"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$team">
                <xsl:attribute name="team"><xsl:value-of select="$team"/></xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select=".//table[@class='playerTableTable tableBody']" mode="team"/>
        </game>
    </xsl:template>

    <xsl:template match="*" mode="team">
        <team>
            <header>
                <xsl:value-of select="normalize-space(tr[contains(@class, 'playerTableBgRowHead')]/td)"/>
            </header>
            <players>
                <xsl:apply-templates select="tr[contains(@class, 'pncPlayerRow')]" mode="player"/>
            </players>
        </team>
    </xsl:template>

    <xsl:template match="*" mode="player">
        <player id="{@id}">
            <player-slot>
                <xsl:value-of select="normalize-space(td[contains(@class, 'playerSlot')])"/>
            </player-slot>
            <player-name>
                <xsl:value-of select="normalize-space(td[contains(@class, 'playertablePlayerName')])"/>
            </player-name>
            <link/>
            <opponent>
                <xsl:value-of select="td[not(@class)]"/>
            </opponent>
            <game-status>
                <xsl:value-of select="normalize-space(td[contains(@class, 'gameStatusDiv')]/*[not(self::span)])"/>
            </game-status>
            <points>
                <xsl:value-of select="normalize-space(td[contains(@class, 'appliedPoints')])"/>
            </points>
        </player>
    </xsl:template>

    <xsl:template match="*">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="@* | text()">
        <xsl:copy-of select="."/>
    </xsl:template>

</xsl:transform>
