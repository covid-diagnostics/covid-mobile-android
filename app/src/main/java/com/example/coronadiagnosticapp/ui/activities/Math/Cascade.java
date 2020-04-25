/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Parallel Colt.
 *
 * The Initial Developer of the Original Code is
 * Piotr Wendykier, Emory University.
 * Portions created by the Initial Developer are Copyright (C) 2007-2009
 * the Initial Developer. All Rights Reserved.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package com.example.coronadiagnosticapp.ui.activities.Math;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  Copyright (c) 2009 by Vinnie Falco
 *  Copyright (c) 2016 by Bernd Porr
 */


import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;

/**
 * The mother of all filters. It contains the coefficients of all
 * filter stages as a sequence of 2nd order filters and the states
 * of the 2nd order filters which also imply if it's direct form I or II
 */
public class Cascade {

    // coefficients
    private Biquad[] m_biquads;

    // the states of the filters
    private DirectFormAbstract[] m_states;

    // number of biquads in the system
    private int m_numBiquads;

    private int numPoles;

    public Cascade() {
        m_numBiquads = 0;
        m_biquads = null;
        m_states = null;
    }

    public int getNumBiquads() {
        return m_numBiquads;
    }

    public Biquad getBiquad(int index) {
        return m_biquads[index];
    }

    public void reset() {
        for (int i = 0; i < m_numBiquads; i++)
            m_states[i].reset();
    }

    public double filter(double in) {
        double out = in;
        for (int i = 0; i < m_numBiquads; i++) {
            if (m_states[i] != null) {
                out = m_states[i].process1(out, m_biquads[i]);
            }
        }
        return out;
    }

    public Complex response(double normalizedFrequency) {
        double w = 2 * Math.PI * normalizedFrequency;
        Complex czn1 = ComplexUtils.polar2Complex(1., -w);
        Complex czn2 = ComplexUtils.polar2Complex(1., -2 * w);
        Complex ch = new Complex(1);
        Complex cbot = new Complex(1);

        for (int i = 0; i < m_numBiquads; i++) {
            Biquad stage = m_biquads[i];
            Complex cb = new Complex(1);
            Complex ct = new Complex(stage.getB0() / stage.getA0());
            ct = MathSupplement.addmul(ct, stage.getB1() / stage.getA0(), czn1);
            ct = MathSupplement.addmul(ct, stage.getB2() / stage.getA0(), czn2);
            cb = MathSupplement.addmul(cb, stage.getA1() / stage.getA0(), czn1);
            cb = MathSupplement.addmul(cb, stage.getA2() / stage.getA0(), czn2);
            ch = ch.multiply(ct);
            cbot = cbot.multiply(cb);
        }

        return ch.divide(cbot);
    }

    public void applyScale(double scale) {
        // For higher order filters it might be helpful
        // to spread this factor between all the stages.
        if (m_biquads.length > 0) {
            m_biquads[0].applyScale(scale);
        }
    }

    public void setLayout(LayoutBase proto, int filterTypes) {
        numPoles = proto.getNumPoles();
        m_numBiquads = (numPoles + 1) / 2;
        m_biquads = new Biquad[m_numBiquads];
        switch (filterTypes) {
            case DirectFormAbstract.DIRECT_FORM_I:
                m_states = new DirectFormI[m_numBiquads];
                for (int i = 0; i < m_numBiquads; i++) {
                    m_states[i] = new DirectFormI();
                }
                break;
            case DirectFormAbstract.DIRECT_FORM_II:
            default:
                m_states = new DirectFormII[m_numBiquads];
                for (int i = 0; i < m_numBiquads; i++) {
                    m_states[i] = new DirectFormII();
                }
                break;
        }
        for (int i = 0; i < m_numBiquads; ++i) {
            PoleZeroPair p = proto.getPair(i);
            m_biquads[i] = new Biquad();
            m_biquads[i].setPoleZeroPair(p);
        }
        applyScale(proto.getNormalGain()
                / ((response(proto.getNormalW() / (2 * Math.PI)))).abs());
    }

}
