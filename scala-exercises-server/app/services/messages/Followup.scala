package services.messages

import shared.Followup

case class CreateFollowupRequest(
    login: String,
    section: String,
    category: String,
    status: String)

case class CreateFollowupResponse(followup: Followup)
