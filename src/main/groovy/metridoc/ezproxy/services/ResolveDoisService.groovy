package metridoc.ezproxy.services

import metridoc.core.InjectArgBase
import metridoc.core.services.RunnableService
import metridoc.ezproxy.entities.EzDoi
import metridoc.ezproxy.entities.EzDoiJournal
import metridoc.ezproxy.utils.TruncateUtils
import metridoc.service.gorm.GormService

/**
 * Created with IntelliJ IDEA on 9/24/13
 * @author Tommy Barker
 */
@InjectArgBase("ezproxy")
class ResolveDoisService extends RunnableService {

    int doiResolutionCount = 2000

    void resolveDois() {
        def gormService = includeService(GormService)
        try {
            gormService.enableFor(EzDoiJournal, EzDoi)
        }
        catch (IllegalStateException ignore) {
            //in case we already enabled the classes
        }

        EzDoi.withTransaction {

            List ezDois = EzDoi.findAllByProcessedDoi(false, [max: doiResolutionCount])

            if (ezDois) {
                println "processing ${ezDois.size()} dois"
            }
            else {
                println "there are no dois to process"
            }

            CrossRefService crossRefTool = includeService(CrossRefService)
            int counter = 0
            ezDois.each { EzDoi ezDoi ->
                counter++
                if (counter % 100 == 0) {
                    println "processed $counter records"
                }
                def response = crossRefTool.resolveDoi(ezDoi.doi)
                assert !response.loginFailure: "Could not login into cross ref"
                if (response.malformedDoi || response.unresolved) {
                    ezDoi.resolvableDoi = false
                    println "Could not resolve doi $ezDoi.doi, it was either malformed or unresolvable"
                }

                else {
                    EzDoiJournal journal = EzDoiJournal.findByDoi(response.doi)
                    if(journal) {
                        println "doi ${response.doi} has already been processed"
                        return
                    }
                    def ezJournal = new EzDoiJournal()
                    ezJournal.properties.findAll {
                        it.key != "id" &&
                        it.key != "version" &&
                        it.key != "class"
                    }.each { key, value ->
                        def chosenValue = response."$key"
                        if (chosenValue instanceof String) {
                            chosenValue = TruncateUtils.truncate(chosenValue, TruncateUtils.DEFAULT_VARCHAR_LENGTH)
                        }

                        ezJournal."$key" = chosenValue
                    }
                    ezJournal.save(failOnError: true)
                }

                ezDoi.processedDoi = true
                ezDoi.save(failOnError: true)
            }
        }
    }

    @Override
    def configure() {
        step(resolveDois: "resolve dois")

        setDefaultTarget("resolveDois")
    }
}
