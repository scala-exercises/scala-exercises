package services.messages

import shared.Followup

case class CreateFollowupRequest(
    text: String)

case class CreateFollowupResponse(followup: Followup)
