from datetime import date

from matching.models import Action, ActionCategory


W_CONTENT = 0.35
W_COLLAB = 0.25
W_IMPLICIT = 0.15
W_BIO = 0.10
W_DISTANCE = 0.15

MAX_DISTANCE_KM = 300
MAX_VIEW_SECONDS = 300


def compute_content_score(active_pet, candidate):
    scores = []

    age_diff = abs(active_pet.age - candidate.age)
    age_score = max(0.0, 1.0 - age_diff / 10)
    scores.append(age_score)

    if active_pet.has_pedigree == candidate.has_pedigree:
        pedigree_score = 1.0
    else:
        pedigree_score = 0.0

    scores.append(pedigree_score)

    return sum(scores) / len(scores)


def compute_implicit_signal(u_id, candidate_id, view_category_id):
    action = (
        Action.objects
        .filter(
            pet_id=candidate_id,
            user_id=u_id,
            category_id=view_category_id
        )
        .order_by('-created_at')
        .first()
    )

    if not action or not action.value:
        return 0.0

    return min(action.value / MAX_VIEW_SECONDS, 1.0)


def compute_bio_compability(candidate):
    if not candidate.last_mating_date:
        return 1.0

    days_passed = (date.today() - candidate.last_mating_date).days

    return min(days_passed / 365, 1.0)


def compute_distance_score(candidate):
    if candidate.distance_km is None:
        return 0.5

    return max(0.0, 1.0 - candidate.distance_km / MAX_DISTANCE_KM)


def score_candidates(active_pet, candidates):
    u_id = active_pet.owner_id
    view_category_id = ActionCategory.objects.filter(name='view').pk

    for candidate in candidates:
        content = compute_content_score(active_pet, candidate)
        collab = getattr(candidate, 'owner_sim', 0.0)
        implicit = compute_implicit_signal(u_id, candidate.id,
                                           view_category_id)
        bio = compute_bio_compability(candidate)
        distance = compute_distance_score(candidate)

        candidate.score = (
            W_CONTENT * content + W_COLLAB * collab + W_IMPLICIT * implicit +
            W_BIO * bio + W_DISTANCE * distance
        )

    return candidates
