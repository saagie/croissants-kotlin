package io.saagie.croissants.dao

import io.saagie.croissants.domain.Proposition
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface PropositionDao : MongoRepository<Proposition, String> {
}