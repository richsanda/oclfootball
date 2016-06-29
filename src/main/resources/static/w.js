$(pageBehavior)

function pageBehavior () {

    var params = decodeURIComponent(window.location.search.substring(1));
    var url = "/games/highestScores";
    if (params != "") { url += "?" + params; }

    $.ajax({
        url: url,
        dataType: "json"
    }).success(function (data) {
        $('#data').append(buildGameList(data));
    });
}

function buildGameList(games) {

    var gamesDiv = $("<div class='games'></div>");

    gamesDiv.append(
        "<div class='headers'>" +
            "<div class='header index'>" + "&#160;" + "</div>" +
            "<div class='header points'>" + "POINTS" + "</div>" +
            "<div class='header team'>" + "OWNER" + "</div>" +
            "<div class='header season'>" + "SEASON" + "</div>" +
            "<div class='header week'>" + "WEEK" + "</div>" +
            "<div class='header outcome'>" + "W/L/T" + "</div>" +
            "<div class='header teamheader'>" + "TEAM" + "</div>" +
            "<div class='header teamheader'>" + "OPPONENT" + "</div>" +
        "</div>"
    );

    for (var i = 0; i < games.length; i++) {
        gamesDiv.append(buildGame(games[i], i + 1));
    }

    return gamesDiv;
}

function buildGame(game, index) {

    var wlt = winLossTie(game);
    var name = owner(game.teamNumber, game.season);

    var gameDiv = $(
        "<div class='game'>" +
            "<div class='block index'>" + index + ". </div>" +
            "<div class='block points'>" + game.points + "</div>" +
            "<div class='block team'>" + name + "</div>" +
            "<div class='block season'>" + game.season + "</div>" +
            "<div class='block week'>" + "week " + game.scoringPeriod + "</div>" +
            "<div class='block outcome'>" + wlt + "</div>" +
        "</div>"
    );

    gameDiv.addClass(name);
    gameDiv.append(buildTeam(game.team));
    gameDiv.append(buildTeam(game.opponent));

    return gameDiv;
}

function buildTeam(team) {

    var teamDiv = $(
        "<div class='block teambox game'>" +
            "<div class='teamname'>" + team.header + "</div>" +
        "</div>"
    );

    var positions = ["QB", "RB", "RB/WR", "WR", "WR/TE", "TE", "D/ST", "K"];
    var players = {};
    for (var i = 0; i < positions.length; i++) {
        players[positions[i]] = [];
    }

    for (var i = 0; i < team.player.length; i++) {
        var player = team.player[i];
        var pos = player["position"];
        if ($.inArray(pos, positions) != -1) {
            players[pos].push(player);
        } else {
            // pass
        }
    }

    for (var i = 0; i < positions.length; i++) {
        var pos = positions[i];
        for (var j = 0; j < players[pos].length; j++) {
            var player = players[pos][j];
            teamDiv.append(buildPlayer(player));
        }
    }

    teamDiv.append(
        $(
            "<div class='player'>" +
                "<div class='block playerpos'/>" +
                "<div class='block playername'/>" +
                "<div class='block playerpts'>" + team.points + "</div>" +
            "</div>"
        ));

    return teamDiv;
}

function buildPlayer(player) {

    var home = player.opponent.size == 0 || player.opponent.charAt(0) != '@';
    var homeSep = home ? " v " : " ";
    var playerName = player.playerName;
    if (player.playerTeam != null) { playerName += ", " + player.playerTeam; }
    var playerGame = player.gameStatus + homeSep + player.opponent;

    var playerDiv = $(
        "<div class='player game'>" +
            "<div class='block playerpos'>" + player["position"] + "</div>" +
            "<div class='block playername'>" + playerName + " <span class='playergame'>(" + playerGame + ")</span></div>" +
            //"<div class='block playergame'>" + player.gameStatus + " vs " + player.opponent + "</div>" +
            "<div class='block playerpts'>" + player.points + "</div>" +
        "</div>"
    );

    return playerDiv;
}

function owner(teamNumber, season) {

    switch (teamNumber) {
        case 1:
            return "trav";
            break;
        case 2:
            return "nick";
            break;
        case 3:
            return "grum";
            break;
        case 4:
            return "justin";
            break;
        case 5:
            return "rux";
            break;
        case 6:
            return "rich";
            break;
        case 7:
            return "greg";
            break;
        case 8:
            return "spoth";
            break;
        case 9:
            return "mike";
            break;
        case 10:
            return "argo";
            break;
        case 11:
            return "bill";
            break;
        case 12:
            return season <= 2015 ? "ruggs" : "dodge";
            break;
    }
}

function winLossTie(game) {
    var base = game.win ? "W" : game.loss ? "L" : "T";
    return game.playoffGame ? base + "**" : base;
}

/*

{
  "season" : 2006,
  "scoringPeriod" : 10,
  "teamNumber" : 7,
  "points" : 166,
  "playoffGame" : false,
  "win" : true,
  "tie" : false,
  "loss" : false,
  "winOrTie" : true,
  "lossOrTie" : false,
  "regularSeasonGame" : true,
  "firstGameOfSeason" : false,
  "team" : [ {
    "header" : "Apponequet Justins Box Score",
    "points" : 166,
    "player" : [ {
      "playerId" : "plyr1097",
      "playerName" : "Adam Vinatieri",
      "playerTeam" : "Ind",
      "position" : "K",
      "opponent" : "Buf",
      "gameStatus" : "W 17-16",
      "points" : 5
    },

 */