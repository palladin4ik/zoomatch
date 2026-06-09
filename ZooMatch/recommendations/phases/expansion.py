from pets.models import Pet
from matching.models import Action, ActionCategory

from geo.utils import parse_location, haversine_distance


ALPHA = 0.6
BETA = 0.4
TOP_K_SIMILAR_USERS = 20


def get_user_breed_portfolio(owner_id):
    return set(
        Pet.objects
        .filter(owner_id=owner_id)
        .values_list('breed_id', flat=True)
    )


def compute_similarity(u_id, v_id, u_breed_ids, like_category_id):
    u_pet_ids = Pet.objects.filter(
        owner_id=u_id,
        breed_id__in=u_breed_ids,
    ).values_list('id', flat=True)

    if not u_pet_ids:
        return 0.0

    total_actions = Action.objects.filter(
        pet_id__in=u_pet_ids
    ).count()

    if total_actions == 0:
        return 0.0

    v_likes = Action.objects.filter(
        pet_id__in=u_pet_ids,
        category_id=like_category_id,
        user_id=v_id
    ).count()

    interaction_score = v_likes / total_actions

    v_breed_ids = get_user_breed_portfolio(v_id)

    if not u_breed_ids or not v_breed_ids:
        jaccard = 0.0
    else:
        intersection = len(u_breed_ids & v_breed_ids)
        union = len(u_breed_ids | v_breed_ids)

        if union > 0:
            jaccard = intersection / union
        else:
            jaccard = 0.0

    return ALPHA * interaction_score + BETA * jaccard


def expand_candidates(active_pet, c0_candidates):
    u_id = active_pet.owner_id
    u_breed_ids = get_user_breed_portfolio(u_id)
    like_category_id = ActionCategory.objects.get(name='like').pk

    c0_ids = {pet.id for pet in c0_candidates}
    c0_owner_ids = {pet.owner_id for pet in c0_candidates}

    candidate_owner_ids = c0_owner_ids - {u_id}

    similarities = []
    for v_id in candidate_owner_ids:
        sim = compute_similarity(u_id, v_id, u_breed_ids, like_category_id)
        if sim > 0:
            similarities.append((v_id, sim))

    similarities.sort(key=lambda x: x[1], reverse=True)
    top_owners = [v_id for v_id, _ in similarities[:TOP_K_SIMILAR_USERS]]

    extra_pets = (
        Pet.objects
        .filter(
            owner_id__in=top_owners,
            animal_type=active_pet.animal_type,
            breed=active_pet.breed,
            is_male=not active_pet.is_male,
            is_active=True
        )
        .exclude(id__in=c0_ids)
        .exclude(location__isnull=True)
        .exclude(location='')
        .select_related('owner', 'animal_type', 'breed')
    )

    active_location = parse_location(active_pet.location)

    for pet in extra_pets:
        if active_location:
            pet_location = parse_location(pet.location)

            if pet_location:
                pet.distance_km = haversine_distance(
                    active_location[0], active_location[1],
                    pet_location[0], pet_location[1]
                )
            else:
                pet.distance_km = None
        else:
            pet.distance_km = None

    owner_sim_map = dict(similarities)
    for pet in c0_candidates:
        pet.owner_sim = owner_sim_map.get(pet.owner_id, 0.0)
    for pet in extra_pets:
        pet.owner_sim = owner_sim_map.get(pet.owner_id, 0.0)

    return c0_candidates + list(extra_pets)
