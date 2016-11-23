/*
 * Copyright (C) 2015 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.graphviz;

import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizV8Engine;
import guru.nidi.graphviz.model.CreationContext;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Label;
import guru.nidi.graphviz.model.Node;

import static guru.nidi.graphviz.attribute.Records.rec;
import static guru.nidi.graphviz.attribute.Records.turn;
import static guru.nidi.graphviz.model.Compass.WEST;
import static guru.nidi.graphviz.model.Factory.*;
import static guru.nidi.graphviz.model.Link.to;

/**
 *
 */
public class PerformanceTest {
    public static void main(String[] args) {
        final long a = System.nanoTime();
        Graphviz.useEngine(new GraphvizV8Engine());
//        Graphviz.useEngine(new GraphvizJdkEngine());
        Graphviz.fromString("graph {a--b}").createSvg();
        final long b = System.nanoTime();
        System.out.printf("init %.0f ms%n", (b - a) / 1e6);

        final PerformanceTest pt = new PerformanceTest();
        print(pt.test());
        for (int k = 0; k < 10; k++) {
            print(pt.test());
        }
        print(pt.test(50));
    }

    private static void print(long[] t) {
//        System.out.printf("init %.0f ms%n", (t[1] - t[0]) / 1e6 );
        System.out.printf("1 %.1f ms%n", (t[2] - t[1]) / 1e6);
        System.out.printf("2 %.1f ms%n", (t[3] - t[2]) / 1e6);
        System.out.printf("3 %.1f ms%n", (t[4] - t[3]) / 1e6);
        System.out.printf("4 %.1f ms%n", (t[5] - t[4]) / 1e6);
        System.out.printf("5 %.1f ms%n", (t[6] - t[5]) / 1e6);
        System.out.printf("6 %.1f ms%n", (t[7] - t[6]) / 1e6);
        System.out.printf("7 %.1f ms%n", (t[8] - t[7]) / 1e6);
    }

    private long[] test(int rounds) {
        long[] sum = new long[9];
        for (int i = 0; i < rounds; i++) {
            final long[] s = test();
            for (int j = 0; j < 9; j++) {
                sum[j] += s[j];
            }
        }
        for (int j = 0; j < 9; j++) {
            sum[j] /= rounds;
        }
        return sum;
    }

    private long[] test() {
        final long[] t = new long[9];
        t[0] = System.nanoTime();
        t[1] = System.nanoTime();
        ex1();
        t[2] = System.nanoTime();
        ex2();
        t[3] = System.nanoTime();
        ex3();
        t[4] = System.nanoTime();
        ex4();
        t[5] = System.nanoTime();
        ex5();
        t[6] = System.nanoTime();
        ex6();
        t[7] = System.nanoTime();
        ex7();
        t[8] = System.nanoTime();
        return t;
    }

    public void ex1() {
        new CreationContext(cc -> {
            final Graph g = graph("ex1").directed().withNodes(
                    node("main").link(
                            node("parse"), node("init"), node("cleanup"), node("printf")),
                    node("parse").link(
                            node("execute")),
                    node("execute").link(
                            node("make_string"), node("printf"), node("compare")),
                    node("init").link(
                            node("make_string")));
            Graphviz.fromGraph(g).createSvg();
        });
    }

    public void ex2() {
        final Node
                init = node("init"),
                execute = node("execute"),
                compare = node("compare").attr(Shape.RECTANGLE, Style.FILLED, Color.hsv(.7, .3, 1.0)),
                make_string = node("make_string"),
                printf = node("printf");
        final Graph g = graph("ex2").directed().withNodes(
                node("main").attr(Shape.RECTANGLE).link(
                        to(node("parse").link(execute)).attr("weight", 8),
                        to(init).attr(Style.DOTTED),
                        node("cleanup"),
                        to(printf).attr(Style.BOLD, Label.of("100 times"), Color.RED)),
                execute.link(graph().withNodes(make_string, printf), to(compare).attr(Color.RED)),
                init.link(make_string.attr(Label.of("make a\nstring"))));
        Graphviz.fromGraph(g).createSvg();
    }

    public void ex3() {
        final Node
                a = node("a").attr(Shape.polygon(5, 0, 0), "peripheries", 3, Color.LIGHTBLUE, Style.FILLED),
                c = node("c").attr(Shape.polygon(4, .4, 0), Label.of("hello world")),
                d = node("d").attr(Shape.INV_TRIANGLE),
                e = node("e").attr(Shape.polygon(4, 0, .7));
        final Graph g = graph("ex3").directed().withNodes(
                a.link(node("b").link(c, d)),
                e);
        Graphviz.fromGraph(g).createSvg();
    }

    public void ex4() {
        final Node
                struct1 = node("struct1").attr(Records.label("<f0> left|<f1> mid\\ dle|<f2> right")),
                struct2 = node("struct2").attr(Records.label("<f0> one|<f1> two")),
                struct3 = node("struct3").attr(Records.label("hello\nworld |{ b |{c|<here> d|e}| f}| g | h"));
        final Graph g = graph("ex41").directed().with(
                struct1.link(
                        between(loc("f1"), struct2.loc("f0")),
                        between(loc("f2"), struct3.loc("here"))));
        Graphviz.fromGraph(g).createSvg();
    }

    public void ex5() {
        final Node
                reiser = node("Reiser cpp"), csh = node("Cshell"),
                ksh = node("ksh"), emacs = node("emacs"),
                vi = node("vi"), build = node("build"),
                bsh = node("Bourne sh"), sccs = node("SCCS"),
                rcs = node("RCS"), make = node("make"),
                yacc = node("yacc"), cron = node("cron"),
                nmake = node("nmake"), ifs = node("IFS"),
                ttu = node("TTU"), cs = node("C*"),
                ncpp = node("ncpp"), kshi = node("ksh-i"),
                curses = node("<curses>"), imx = node("IMX"),
                syned = node("SYNED"), cursesi = node("<curses-i>"),
                pg2 = node("PG2"), peggy = node("Peggy"),
                dag = node("DAG"), csas = node("CSAS"),
                ansiCpp = node("Ansi cpp"), fdelta = node("fdelta"),
                d3fs = node("3D File System"), nmake2 = node("nmake 2.0"),
                cia = node("CIA"), sbcs = node("SBCS"),
                pax = node("PAX"), ksh88 = node("ksh-88"),
                pegasus = node("PEGASUS/PML"), ciapp = node("CIA++"),
                app = node("APP"), ship = node("SHIP"),
                dataShare = node("DataShare"), ryacc = node("Ryacc"),
                mosaic = node("Mosaic"), backtalk = node("backtalk"),
                dot = node("DOT"), dia = node("DIA"),
                libft = node("libft"), coshell = node("CoShell"),
                sfio = node("sfio"), ifsi = node("IFS-i"),
                mlx = node("ML-X"), kyacc = node("kyacc"),
                yeast = node("yeast"), sis = node("Software IS"),
                cfg = node("Configuration Mgt"), archlib = node("Architecture & Libraries"),
                proc = node("Process"), adv = node("Adv. Software Technology");

        final Graph g = graph("ex5").directed()
                .general().attr("ranksep", .75, "size", "7.5,7.5")
                .nodes().attr(Shape.RECTANGLE)
                .withGraphs(
                        graph().nodes().attr(Shape.NONE).with(
                                node("past").link(
                                        node("1978").link(
                                                node("1980").link(
                                                        node("1982").link(
                                                                node("1983").link(
                                                                        node("1985").link(
                                                                                node("1986").link(
                                                                                        node("1987").link(
                                                                                                node("1988").link(
                                                                                                        node("1989").link(
                                                                                                                node("1990").link(
                                                                                                                        node("future")))))))))))))
                                .withNodes(bsh, make, sccs, reiser, csh, yacc, cron, rcs, emacs, build, vi, curses),
                        graph().general().attr(Rank.SAME).nodes().attr(Shape.ELLIPSE).withNodes(sis, cfg, archlib, proc),
                        graph().general().attr(Rank.SAME).withNodes("past").withNodes(sccs, make, bsh, yacc, cron),
                        graph().general().attr(Rank.SAME).withNodes("1978").withNodes(reiser, csh),
                        graph().general().attr(Rank.SAME).withNodes("1980").withNodes(build, emacs, vi),
                        graph().general().attr(Rank.SAME).withNodes("1982").withNodes(rcs, curses, imx, syned),
                        graph().general().attr(Rank.SAME).withNodes("1983").withNodes(ksh, ifs, ttu),
                        graph().general().attr(Rank.SAME).withNodes("1985").withNodes(nmake, peggy),
                        graph().general().attr(Rank.SAME).withNodes("1986").withNodes(cs, ncpp, kshi, cursesi, pg2),
                        graph().general().attr(Rank.SAME).withNodes("1987").withNodes(dag, csas, ansiCpp, fdelta, d3fs, nmake2),
                        graph().general().attr(Rank.SAME).withNodes("1988").withNodes(cia, sbcs, pax, ksh88, pegasus, backtalk),
                        graph().general().attr(Rank.SAME).withNodes("1989").withNodes(ciapp, app, ship, dataShare, ryacc, mosaic),
                        graph().general().attr(Rank.SAME).withNodes("1990").withNodes(dot, dia, libft, coshell, sfio, ifsi, mlx, kyacc, yeast),
                        graph().general().attr(Rank.SAME).withNodes("future").with(adv))
                .withNodes(
                        sccs.link(rcs, nmake, d3fs),
                        make.link(build, nmake),
                        build.link(nmake2),
                        bsh.link(csh.link(ksh), ksh),
                        reiser.link(ncpp),
                        vi.link(curses, ksh),
                        emacs.link(ksh),
                        rcs.link(fdelta, sbcs),
                        curses.link(cursesi),
                        syned.link(peggy),
                        imx.link(ttu),
                        ksh.link(nmake, ksh88),
                        ifs.link(cursesi, sfio, ifsi),
                        ttu.link(pg2),
                        nmake.link(ksh, ncpp, d3fs, nmake2),
                        cs.link(csas),
                        ncpp.link(ansiCpp),
                        cursesi.link(fdelta),
                        csas.link(cia),
                        fdelta.link(sbcs, pax),
                        kshi.link(ksh88),
                        peggy.link(pegasus, ryacc),
                        pg2.link(backtalk),
                        cia.link(ciapp, dia),
                        pax.link(ship),
                        backtalk.link(dataShare),
                        yacc.link(ryacc),
                        dag.link(dot, dia, sis),
                        app.link(dia, sis),
                        nmake2.link(coshell, cfg),
                        ksh88.link(sfio, coshell, archlib),
                        pegasus.link(mlx, archlib),
                        ryacc.link(kyacc),
                        cron.link(yeast),
                        dot.link(sis),
                        ciapp.link(sis),
                        dia.link(sis),
                        libft.link(sis),
                        ansiCpp.link(cfg),
                        sbcs.link(cfg),
                        ship.link(cfg),
                        d3fs.link(cfg),
                        coshell.link(cfg, archlib),
                        sfio.link(archlib),
                        ifsi.link(archlib),
                        mlx.link(archlib),
                        dataShare.link(archlib),
                        kyacc.link(archlib),
                        mosaic.link(proc),
                        yeast.link(proc),
                        sis.link(adv),
                        cfg.link(adv),
                        archlib.link(adv),
                        proc.link(adv)
                );
        Graphviz.fromGraph(g).createSvg();
    }

    public void ex6() {
        final Node
                node0 = node("node0").attr(Records.of(rec("f0", ""), rec("f1", ""), rec("f2", ""), rec("f3", ""), rec("f4", ""), rec("f5", ""), rec("f6", ""))),
                node1 = node("node1").attr(Records.of(turn(rec("n", "n14"), rec("719"), rec("p", "")))),
                node2 = node("node2").attr(Records.of(turn(rec("n", "a1"), rec("805"), rec("p", "")))),
                node3 = node("node3").attr(Records.of(turn(rec("n", "i9"), rec("718"), rec("p", "")))),
                node4 = node("node4").attr(Records.of(turn(rec("n", "e5"), rec("989"), rec("p", "")))),
                node5 = node("node5").attr(Records.of(turn(rec("n", "t20"), rec("959"), rec("p", "")))),
                node6 = node("node6").attr(Records.of(turn(rec("n", "o15"), rec("794"), rec("p", "")))),
                node7 = node("node7").attr(Records.of(turn(rec("n", "s19"), rec("659"), rec("p", ""))));
        final Graph g = graph("ex6").directed()
                .general().attr(RankDir.LEFT_TO_RIGHT)
                .withNodes(
                        node0.link(
                                between(loc("f0"), node1.loc(WEST)),
                                between(loc("f1"), node2.loc(WEST)),
                                between(loc("f2"), node3.loc(WEST)),
                                between(loc("f5"), node4.loc(WEST)),
                                between(loc("f6"), node5.loc(WEST))),
                        node2.link(between(loc("p"), node6.loc(WEST))),
                        node4.link(between(loc("p"), node7.loc(WEST))));
        Graphviz.fromGraph(g).createSvg();
    }

    public void ex7() {
        final Graph g = graph("ex7").directed()
                .withGraphs(
                        graph().cluster()
                                .nodes().attr(Style.FILLED, Color.WHITE)
                                .general().attr(Style.FILLED, Color.LIGHTGREY, Label.of("process #1"))
                                .with(node("a0").link(node("a1").link(node("a2").link(node("a3"))))),
                        graph("x").cluster()
                                .nodes().attr(Style.FILLED)
                                .general().attr(Color.BLUE, Label.of("process #2"))
                                .with(node("b0").link(node("b1").link(node("b2").link(node("b3"))))))
                .withNodes(
                        node("start").attr(Shape.mDiamond("", "")).link("a0", "b0"),
                        node("a1").link("b3"),
                        node("b2").link("a3"),
                        node("a3").link("a0"),
                        node("a3").link("end"),
                        node("b3").link("end"),
                        node("end").attr(Shape.mSquare("", ""))
                );
        Graphviz.fromGraph(g).createSvg();
    }

}