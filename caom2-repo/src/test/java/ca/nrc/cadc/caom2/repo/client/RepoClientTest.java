/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2011.                            (c) 2011.
*  Government of Canada                 Gouvernement du Canada
*  National Research Council            Conseil national de recherches
*  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
*  All rights reserved                  Tous droits réservés
*
*  NRC disclaims any warranties,        Le CNRC dénie toute garantie
*  expressed, implied, or               énoncée, implicite ou légale,
*  statutory, of any kind with          de quelque nature que ce
*  respect to the software,             soit, concernant le logiciel,
*  including without limitation         y compris sans restriction
*  any warranty of merchantability      toute garantie de valeur
*  or fitness for a particular          marchande ou de pertinence
*  purpose. NRC shall not be            pour un usage particulier.
*  liable in any event for any          Le CNRC ne pourra en aucun cas
*  damages, whether direct or           être tenu responsable de tout
*  indirect, special or general,        dommage, direct ou indirect,
*  consequential or incidental,         particulier ou général,
*  arising from the use of the          accessoire ou fortuit, résultant
*  software.  Neither the name          de l'utilisation du logiciel. Ni
*  of the National Research             le nom du Conseil National de
*  Council of Canada nor the            Recherches du Canada ni les noms
*  names of its contributors may        de ses  participants ne peuvent
*  be used to endorse or promote        être utilisés pour approuver ou
*  products derived from this           promouvoir les produits dérivés
*  software without specific prior      de ce logiciel sans autorisation
*  written permission.                  préalable et particulière
*                                       par écrit.
*
*  This file is part of the             Ce fichier fait partie du projet
*  OpenCADC project.                    OpenCADC.
*
*  OpenCADC is free software:           OpenCADC est un logiciel libre ;
*  you can redistribute it and/or       vous pouvez le redistribuer ou le
*  modify it under the terms of         modifier suivant les termes de
*  the GNU Affero General Public        la “GNU Affero General Public
*  License as published by the          License” telle que publiée
*  Free Software Foundation,            par la Free Software Foundation
*  either version 3 of the              : soit la version 3 de cette
*  License, or (at your option)         licence, soit (à votre gré)
*  any later version.                   toute version ultérieure.
*
*  OpenCADC is distributed in the       OpenCADC est distribué
*  hope that it will be useful,         dans l’espoir qu’il vous
*  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
*  without even the implied             GARANTIE : sans même la garantie
*  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
*  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
*  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
*  General Public License for           Générale Publique GNU Affero
*  more details.                        pour plus de détails.
*
*  You should have received             Vous devriez avoir reçu une
*  a copy of the GNU Affero             copie de la Licence Générale
*  General Public License along         Publique GNU Affero avec
*  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
*  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
*                                       <http://www.gnu.org/licenses/>.
*
*  $Revision: 5 $
*
************************************************************************
*/

package ca.nrc.cadc.caom2.repo.client;

import ca.nrc.cadc.auth.AuthenticationUtil;
import ca.nrc.cadc.auth.RunnableAction;
import ca.nrc.cadc.caom2.ObservationState;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import ca.nrc.cadc.caom2.ObservationState;
import ca.nrc.cadc.caom2.ObservationURI;
import ca.nrc.cadc.net.NetrcAuthenticator;
import ca.nrc.cadc.util.Log4jInit;
import java.security.AccessControlException;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.security.auth.Subject;

public class RepoClientTest
{

    private static final Logger log = Logger.getLogger(RepoClientTest.class);

    static
    {
        Log4jInit.setLevel("ca.nrc.cadc.caom2.repo.client.RepoClient", Level.DEBUG);
    }

    // @Test
    public void testTemplate()
    {
        try
        {

        }
        catch (Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    @Test
    public void testGetObservationList()
    {
        try
        {
            Subject s = AuthenticationUtil.getSubject(new NetrcAuthenticator(true));
            Subject.doAs(s, new PrivilegedExceptionAction<Object>()
                    {
                        public Object run()
                            throws Exception
                        {
                            RepoClient repoC = new RepoClient(URI.create("ivo://cadc.nrc.ca/caom2repo"), 8);

                            List<ObservationState> list = repoC.getObservationList("IRIS", null, null, 5);
                            Assert.assertEquals(list.size(), 5);
                            Assert.assertEquals(URI.create("caom:IRIS/f001h000"), list.get(0).getURI().getURI());
                            Assert.assertEquals(URI.create("caom:IRIS/f002h000"), list.get(1).getURI().getURI());
                            Assert.assertEquals(URI.create("caom:IRIS/f003h000"), list.get(2).getURI().getURI());
                            Assert.assertEquals(URI.create("caom:IRIS/f004h000"), list.get(3).getURI().getURI());
                            Assert.assertEquals(URI.create("caom:IRIS/f005h000"), list.get(4).getURI().getURI());
                            
                            return null;
                        }
                    });
        }
        catch (Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }
    
    @Test
    public void testGetObservationListDenied()
    {
        try
        {
            Subject s = AuthenticationUtil.getAnonSubject();
            Subject.doAs(s, new PrivilegedExceptionAction<Object>()
                    {
                        public Object run()
                            throws Exception
                        {
                            RepoClient repoC = new RepoClient(URI.create("ivo://cadc.nrc.ca/caom2repo"), 8);

                            List<ObservationState> list = repoC.getObservationList("IRIS", null, null, 5);
                            Assert.fail("expected exception, got results");
                            
                            return null;
                        }
                    });
        }
        catch(AccessControlException expected)
        {
            log.info("caught expected exception: " + expected);
        }
        catch (Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    @Test
    public void testGetList()
    {
        try
        {
            Subject s = AuthenticationUtil.getSubject(new NetrcAuthenticator(true));
            Subject.doAs(s, new PrivilegedExceptionAction<Object>()
                    {
                        public Object run()
                            throws Exception
                        {
                            RepoClient repoC = new RepoClient(URI.create("ivo://cadc.nrc.ca/caom2repo"), 8);

                            List<WorkerResponse> list = repoC.getList("IRIS", null, null, 5);
                            Assert.assertEquals(list.size(), 5);
                            Assert.assertEquals(URI.create("caom:IRIS/f001h000"), list.get(0).getObservation().getURI().getURI());
                            Assert.assertEquals(URI.create("caom:IRIS/f002h000"), list.get(1).getObservation().getURI().getURI());
                            Assert.assertEquals(URI.create("caom:IRIS/f003h000"), list.get(2).getObservation().getURI().getURI());
                            Assert.assertEquals(URI.create("caom:IRIS/f004h000"), list.get(3).getObservation().getURI().getURI());
                            Assert.assertEquals(URI.create("caom:IRIS/f005h000"), list.get(4).getObservation().getURI().getURI());
                            
                            return null;
                        }
                    });
        }
        catch (Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

    @Test
    public void testGet()
    {
        try
        {
            Subject s = AuthenticationUtil.getSubject(new NetrcAuthenticator(true));
            Subject.doAs(s, new PrivilegedExceptionAction<Object>()
                    {
                        public Object run()
                            throws Exception
                        {
                            RepoClient repoC = new RepoClient(URI.create("ivo://cadc.nrc.ca/caom2repo"), 8);

                            WorkerResponse wr = repoC.get(new ObservationURI("IRIS", "f001h000"));
                            Assert.assertEquals(wr.getObservation().getID().toString(), "00000000-0000-0000-897c-013ac26a8f32");
                            
                            return null;
                        }
                    });
        }
        catch (Exception unexpected)
        {
            log.error("unexpected exception", unexpected);
            Assert.fail("unexpected exception: " + unexpected);
        }
    }

}