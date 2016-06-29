<xsl:transform
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:o="urn:schemas-microsoft-com:office:office"
        xmlns:x="urn:schemas-microsoft-com:office:excel"
        xmlns:dt="uuid:C2F41010-65B3-11d1-A29F-00AA00C14882"
        xmlns:s="uuid:BDC6E3F0-6DA3-11d1-A2A3-00AA00C14882"
        xmlns:rs="urn:schemas-microsoft-com:rowset"
        xmlns:z="#RowsetSchema"
        xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
        xmlns:html="http://www.w3.org/TR/REC-html40"

        exclude-result-prefixes="xsl o x dt s rs z ss html"

        version="2.0">

    <xsl:output method="xml"
                omit-xml-declaration="yes"
                indent="yes"/>

    <xsl:strip-space elements="*"/>

    <xsl:template match="/">

        <playerweeks>
            <xsl:apply-templates select="/ss:Workbook/ss:Worksheet[@ss:Name = 'BOX SCORES']/ss:Table/ss:Row[position() != 1]"/>
        </playerweeks>
    </xsl:template>

    <xsl:template match="ss:Row">

        <!--

            <Cell ss:StyleID="s70"><Data ss:Type="String">Total</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell ss:StyleID="s70"><Data ss:Type="String">Division</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell ss:StyleID="s70"><Data ss:Type="String">Year</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell ss:StyleID="s70"><Data ss:Type="String">Week</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell ss:StyleID="s70"><Data ss:Type="String">Owner</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell ss:StyleID="s70"><Data ss:Type="String">Opponent</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell ss:StyleID="s70"><Data ss:Type="String">POS</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell ss:StyleID="s70"><Data ss:Type="String">Player</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell ss:StyleID="s70"><Data ss:Type="String">Ppos</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell ss:StyleID="s70"><Data ss:Type="String">Pts</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell><Data ss:Type="String">a</Data><NamedCell ss:Name="_FilterDatabase"/></Cell>
    <Cell><Data ss:Type="String">b</Data><NamedCell ss:Name="_FilterDatabase"/></Cell>
    <Cell><Data ss:Type="String">Potential</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell><Data ss:Type="String">Managerial %</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell ss:StyleID="s70"><Data ss:Type="String">R</Data><NamedCell
      ss:Name="_FilterDatabase"/></Cell>
    <Cell ss:StyleID="s70"/>

        -->

        <xsl:variable name="firstCellIndex" select="ss:Cell[1]/@ss:Index"/>

        <xsl:variable name="total" select="ss:Cell[1]/ss:Data"/>
        <xsl:variable name="division" select="ss:Cell[1]/ss:Data"/>
        <xsl:variable name="year" select="ss:Cell[2]/ss:Data"/>
        <xsl:variable name="week" select="ss:Cell[3]/ss:Data"/>
        <xsl:variable name="owner" select="ss:Cell[4]/ss:Data"/>
        <xsl:variable name="opponent" select="ss:Cell[5]/ss:Data"/>
        <xsl:variable name="pos" select="ss:Cell[6]/ss:Data"/>
        <xsl:variable name="player" select="ss:Cell[7]/ss:Data"/>
        <xsl:variable name="ppos" select="ss:Cell[8]/ss:Data"/>
        <xsl:variable name="points" select="ss:Cell[9]/ss:Data"/>

        <xsl:if test="$opponent != 'BENCH POINTS:'">
            <playerweek>
                <!--total><xsl:value-of select="$total"/></total-->
                <division><xsl:value-of select="$division"/></division>
                <year><xsl:value-of select="$year"/></year>
                <week><xsl:value-of select="$week"/></week>
                <owner><xsl:value-of select="$owner"/></owner>
                <opponent><xsl:value-of select="$opponent"/></opponent>
                <position><xsl:value-of select="$pos"/></position>
                <player><xsl:value-of select="$player"/></player>
                <ppos><xsl:value-of select="$ppos"/></ppos>
                <points><xsl:value-of select="$points"/></points>
            </playerweek>
        </xsl:if>

    </xsl:template>

</xsl:transform>
