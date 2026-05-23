from datetime import date

from pets.models import Pet
from geo.utils import parse_location, haversine_distance


RECOVERY_DAYS = {
    1: 90,
    2: 180,
}
DEFAULT_RECOVERY_DAYS = 120

RADIUS_CHOICES = (50, 150, 300)


def get_recovery_days(animal_type_id):
    return RECOVERY_DAYS.get(animal_type_id, DEFAULT_RECOVERY_DAYS)


def is_biologically_ready(pet):
    if not pet.last_mating_date or pet.is_male:
        return True

    recovery = get_recovery_days(pet.animal_type_id)
    days_passed = (date.today() - pet.last_mating_date).days

    return days_passed >= recovery


def generate_candidates(active_pet, radius_km=None):
    qs = (
        Pet.objects
        .filter(
            animal_type=active_pet.animal_type,
            breed=active_pet.breed,
            is_male=not active_pet.is_male,
            is_active=True,
        )
        .exclude(owner=active_pet.owner)
        .exclude(location__isnull=True)
        .exclude(location='')
        .select_related('owner', 'animal_type', 'breed')
    )

    active_location = parse_location(active_pet.location)

    candidates = []

    for pet in qs:
        if not is_biologically_ready(pet):
            continue

        if active_location and radius_km in RADIUS_CHOICES:
            pet_location = parse_location(pet.location)

            if not pet_location:
                continue

            distance = haversine_distance(
                active_location[0], active_location[1],
                pet_location[0], pet_location[1]
            )

            if distance > radius_km:
                continue

            pet.distance_km = distance
        else:
            pet.distance_km = None

        candidates.append(pet)

    return candidates
