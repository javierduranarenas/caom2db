package ca.nrc.cadc.caom2.harvester;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import ca.nrc.cadc.caom2.DeletedObservation;
import ca.nrc.cadc.caom2.DeletedObservationMetaReadAccess;
import ca.nrc.cadc.caom2.DeletedPlaneDataReadAccess;
import ca.nrc.cadc.caom2.DeletedPlaneMetaReadAccess;
import ca.nrc.cadc.caom2.access.ObservationMetaReadAccess;
import ca.nrc.cadc.caom2.access.PlaneDataReadAccess;
import ca.nrc.cadc.caom2.access.PlaneMetaReadAccess;
import ca.nrc.cadc.caom2.harvester.state.HarvestState;
import ca.nrc.cadc.caom2.version.InitDatabase;
import ca.nrc.cadc.db.ConnectionConfig;
import ca.nrc.cadc.db.DBConfig;
import ca.nrc.cadc.db.DBUtil;

/**
 * A wrapper that calls the Harvester implementations in the right order.
 *
 * @author pdowler
 */
public class CaomHarvester implements Runnable
{
    /**
     * log
     */
    private static Logger log = Logger.getLogger(CaomHarvester.class);
    /**
     * initdb
     */
    private InitDatabase initdb;
    /**
     * obsHarvester
     */
    private ObservationHarvester obsHarvester;
    /**
     * obsDeleter
     */
    private DeletionHarvester obsDeleter;
    /**
     * observationMetaHarvester
     */
    private ReadAccessHarvester observationMetaHarvester;
    /**
     * planeDataHarvester
     */
    private ReadAccessHarvester planeDataHarvester;
    /**
     * planeMetaHarvester
     */
    private ReadAccessHarvester planeMetaHarvester;
    /**
     * observationMetaDeleter
     */
    private DeletionHarvester observationMetaDeleter;
    /**
     * planeDataDeleter
     */
    private DeletionHarvester planeDataDeleter;
    /**
     * planeDataDeleter
     */
    private DeletionHarvester planeMetaDeleter;

    /**
     * Harvest everything.
     *
     * @param dryrun
     *            true if no changed in the data base are applied during the
     *            process
     * @param compute compute plane metadata from WCS before insert
     * @param src
     *            source server,database,schema
     * @param dest
     *            destination server,database,schema
     * @param batchSize
     *            number of observations per batch (~memory consumption)
     * @param batchFactor
     *            multiplier for batchSize when harvesting single-table entities
     * @param full
     *            full harvest of all source entities
     * @param skip
     *            flag that indicates if shipped observations should be dealt
     * @param maxDate
     *            latest date to be using during harvester
     * @throws java.io.IOException
     *             IOException
     * @throws URISyntaxException
     *             URISyntaxException
     */
    public CaomHarvester(boolean dryrun, boolean compute, String[] src, String[] dest, int batchSize, int batchFactor, boolean full,
            boolean skip, Date maxDate) throws IOException, URISyntaxException
    {
        Integer entityBatchSize = batchSize * batchFactor;

        DBConfig dbrc = new DBConfig();
        ConnectionConfig cc = dbrc.getConnectionConfig(dest[0], dest[1]);
        DataSource ds = DBUtil.getDataSource(cc);
        this.initdb = new InitDatabase(ds, dest[1], dest[2]);

        this.obsHarvester = new ObservationHarvester(src, dest, batchSize, full, dryrun);
        obsHarvester.setSkipped(skip);
        obsHarvester.setMaxDate(maxDate);
        obsHarvester.setComputePlaneMetadata(compute);

        this.observationMetaHarvester = new ReadAccessHarvester(ObservationMetaReadAccess.class, src, dest,
                entityBatchSize, full, dryrun);
        observationMetaHarvester.setSkipped(skip);
        this.planeDataHarvester = new ReadAccessHarvester(PlaneDataReadAccess.class, src, dest, entityBatchSize, full,
                dryrun);
        planeDataHarvester.setSkipped(skip);
        this.planeMetaHarvester = new ReadAccessHarvester(PlaneMetaReadAccess.class, src, dest, entityBatchSize, full,
                dryrun);
        planeMetaHarvester.setSkipped(skip);

        if (!full)
        {
            this.obsDeleter = new DeletionHarvester(DeletedObservation.class, src, dest, entityBatchSize, dryrun);

            if (!skip)
            {
                this.observationMetaDeleter = new DeletionHarvester(DeletedObservationMetaReadAccess.class, src, dest,
                        entityBatchSize, dryrun);
                this.planeMetaDeleter = new DeletionHarvester(DeletedPlaneMetaReadAccess.class, src, dest,
                        entityBatchSize, dryrun);
                this.planeDataDeleter = new DeletionHarvester(DeletedPlaneDataReadAccess.class, src, dest,
                        entityBatchSize, dryrun);
            }
        }
    }

    /**
     * Harvest everything.
     *
     * @param dryrun
     *            true if no changed in the data base are applied during the
     *            process
     * @param compute compute plane metadata from WCS before insert
     * @param resourceId
     *            repo service
     * @param collection
     *            collection to be harvested
     * @param nthreads
     *            number of threads to be used to harvest
     * @param dest
     *            destination server,database,schema
     * @param batchSize
     *            number of observations per batch (~memory consumption)
     * @param batchFactor
     *            multiplier for batchSize when harvesting single-table entities
     * @param full
     *            full harvest of all source entities
     * @param skip
     *            flag that indicates if shipped observations should be dealt
     * @param maxDate
     *            latest date to be using during harvester
     * @throws java.io.IOException
     *             IOException
     * @throws URISyntaxException
     *             URISyntaxException
     */
    public CaomHarvester(boolean dryrun, boolean compute, String resourceId, String collection, int nthreads, String[] dest,
            int batchSize, int batchFactor, boolean full, boolean skip, Date maxDate)
            throws IOException, URISyntaxException
    {
        DBConfig dbrc = new DBConfig();
        ConnectionConfig cc = dbrc.getConnectionConfig(dest[0], dest[1]);
        DataSource ds = DBUtil.getDataSource(cc);
        this.initdb = new InitDatabase(ds, dest[1], dest[2]);

        this.obsHarvester = new ObservationHarvester(resourceId, collection, nthreads, dest, batchSize, full, dryrun);
        obsHarvester.setSkipped(skip);
        obsHarvester.setMaxDate(maxDate);
        obsHarvester.setComputePlaneMetadata(compute);

        if (!full)
        {
            // TODO uncomment when delete service in place
            // this.obsDeleter = new DeletionHarvester(DeletedObservation.class,
            // resourceId, collection, nthreads, dest, entityBatchSize,
            // dryrun);

            if (!skip)
            {
                // TODO uncomment when delete service in place
                // this.observationMetaDeleter = new
                // DeletionHarvester(DeletedObservationMetaReadAccess.class,
                // resourceId,
                // collection, nthreads, dest, entityBatchSize, dryrun);
            }
        }
    }

    /**
     * Harvest everything.
     *
     * @param dryrun
     *            true if no changed in the data base are applied during the
     *            process
     * @param compute compute plane metadata from WCS before insert
     * @param resourceId
     *            repo service
     * @param collection
     *            collection to be harvested
     * @param nthreads
     *            number of threads to be used to harvest
     * @param batchSize
     *            number of observations per batch (~memory consumption)
     * @param dest
     *            destination server,database,schema
     * @param full
     *            full harvest of all source entities
     * @param maxDate
     *            latest date to be used during harvester
     * @throws java.io.IOException
     *             IOException
     * @throws URISyntaxException
     *             URISyntaxException
     */
    public CaomHarvester(boolean dryrun, boolean compute, String resourceId, String collection, int nthreads, String[] dest,
            Integer batchSize, boolean full, Date maxDate) throws IOException, URISyntaxException
    {
        this.obsHarvester = new ObservationHarvester(resourceId, collection, nthreads, dest, batchSize, full, dryrun);
        obsHarvester.setMaxDate(maxDate);
        obsHarvester.setDoCollisionCheck(true);
        obsHarvester.setComputePlaneMetadata(compute);
    }

    /**
     * Harvest observations from source database..
     *
     * @param dryrun
     *            true if no changed in the data base are applied during the
     *            process
     * @param compute compute plane metadata from WCS before insert
     * @param src
     *            source server,database,schema
     * @param dest
     *            destination server,database,schema
     * @param batchSize
     *            number of observations per batch (~memory consumption)
     * @param full
     *            full harvest of all source entities
     * @param maxDate
     *            latest date to be used during harvester
     * @throws java.io.IOException
     *             IOException
     * @throws URISyntaxException
     *             URISyntaxException
     */
    public CaomHarvester(boolean dryrun, boolean compute, String[] src, String[] dest, Integer batchSize, boolean full, Date maxDate)
            throws IOException, URISyntaxException
    {
        this.obsHarvester = new ObservationHarvester(src, dest, batchSize, full, dryrun);
        obsHarvester.setMaxDate(maxDate);
        obsHarvester.setDoCollisionCheck(true);
        obsHarvester.setComputePlaneMetadata(compute);
    }

    // undocumented for use be developers that want to setup a CaomHarvester with only some components or hard-coded
    // config not supported by command-line arguments
    public static CaomHarvester getTestHarvester(boolean dryrun, boolean compute, String[] src, String[] dest, Integer batchSize,
            Integer batchFactor, boolean full, boolean skip, Date maxDate) throws IOException, URISyntaxException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * run
     */
    @Override
    public void run()
    {

        if (obsHarvester.getComputePlaneMetadata())
        {
            // make sure wcslib can be loaded
            try
            {
                log.info("loading ca.nrc.cadc.wcs.WCSLib");
                Class.forName("ca.nrc.cadc.wcs.WCSLib");
            }
            catch (Throwable t)
            {
                throw new RuntimeException("FATAL - failed to load WCSLib JNI binding", t);
            }
        }

        boolean init = false;
        if (initdb != null)
        {
            boolean created = initdb.doInit();
            if (created)
                init = true; // database is empty so can bypass processing old deletions
        }

        // clean up old access control tuples before harvest to avoid conflicts
        // from delete+create
        if (observationMetaDeleter != null)
        {
            boolean initDel = init;
            if (!init)
            {
                log.warn("in observationMetaDeleter != null and !init");

                // check if we have ever harvested before
                HarvestState hs = observationMetaHarvester.harvestState.get(observationMetaHarvester.source,
                        observationMetaHarvester.cname);
                initDel = (hs.curID == null && hs.curLastModified == null); // never
                                                                            // harvested
                                                                            // from
                                                                            // source
                                                                            // before
            }
            observationMetaDeleter.setInitHarvestState(initDel);
            observationMetaDeleter.run();
            log.info("init: " + observationMetaDeleter.cname);
        }
        if (planeDataDeleter != null)
        {
            boolean initDel = init;
            if (!init)
            {
                // check if we have ever harvested before
                HarvestState hs = planeDataHarvester.harvestState.get(planeDataHarvester.source,
                        planeDataHarvester.cname);
                initDel = (hs.curID == null && hs.curLastModified == null); // never
                                                                            // harvested
                                                                            // from
                                                                            // source
                                                                            // before
            }
            planeDataDeleter.setInitHarvestState(initDel);
            planeDataDeleter.run();
            log.info("init: " + planeDataDeleter.cname);
        }
        if (planeMetaDeleter != null)
        {
            boolean initDel = init;
            if (!init)
            {
                // check if we have ever harvested before
                HarvestState hs = planeMetaHarvester.harvestState.get(planeMetaHarvester.source,
                        planeMetaHarvester.cname);
                initDel = (hs.curID == null && hs.curLastModified == null); // never
                                                                            // harvested
                                                                            // from
                                                                            // source
                                                                            // before
            }
            planeMetaDeleter.setInitHarvestState(initDel);
            planeMetaDeleter.run();
            log.info("init: " + planeMetaDeleter.cname);
        }

        // delete observations before harvest to avoid observationURI conflicts
        // from delete+create
        if (obsDeleter != null)
        {
            boolean initDel = init;
            if (!init)
            {
                // check if we have ever harvested before
                HarvestState hs = obsHarvester.harvestState.get(obsHarvester.source, obsHarvester.cname);
                initDel = (hs.curID == null && hs.curLastModified == null); // never
                                                                            // harvested
                                                                            // from
                                                                            // source
                                                                            // before
            }
            log.info("init: " + obsDeleter.source + " " + obsDeleter.cname);
            obsDeleter.setInitHarvestState(initDel);
            obsDeleter.run();
        }

        // harvest observations
        if (obsHarvester != null)
        {
            log.debug("************** obsHarvester.run() ***************");
            obsHarvester.run();
        }

        // make sure access control tuples are harvested after observations
        // because they update asset tables and fail if asset is missing
        if (observationMetaHarvester != null)
        {
            observationMetaHarvester.run();
        }
        if (planeDataHarvester != null)
        {
            planeDataHarvester.run();
        }
        if (planeMetaHarvester != null)
        {
            planeMetaHarvester.run();
        }

    }
}