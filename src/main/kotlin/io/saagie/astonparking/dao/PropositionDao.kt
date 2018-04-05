package io.saagie.astonparking.dao

import io.saagie.astonparking.domain.Proposition
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface PropositionDao : MongoRepository<Proposition, String> {
}